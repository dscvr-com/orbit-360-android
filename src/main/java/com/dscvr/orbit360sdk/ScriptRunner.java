package com.dscvr.orbit360sdk;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ScriptRunner {

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<?> lastSubmittedScript = null;
    private MotorControl control;
    private long currentStartTime;
    private Point2f currentMovement;
    private Point2f currentSpeed;
    private Point2f position;

    public ScriptRunner(MotorControl control) {
        this.control = control;
        this.position = new Point2f(0, 0);
        this.currentSpeed = new Point2f(0, 0);
        this.currentMovement = new Point2f(0, 0);
    }

    public void runScript(List<Command> commands, ExecutionFinishedHandler handler) {
        if (lastSubmittedScript == null || lastSubmittedScript.isDone())
            lastSubmittedScript = executor.submit(new CommandWorkerRunnable(commands, handler));
        else{
            throw new IllegalStateException("there is already a circle Command Running");
        }
    }

    public Point2f getPositionSteps() {
        return new Point2f(getXPositionSteps(), getYPositionSteps());
    }

    public Point2f getPosition() {
        return new Point2f(getXPositionSteps(), getYPositionSteps()).div(MotorControl.DEGREES_TO_STEPS);
    }

    private float getXPositionSteps() {
        return (float)(position.getX() + Math.signum(currentMovement.getX()) * Math.min(Math.abs(currentMovement.getX()), currentSpeed.getX() * (System.currentTimeMillis() - currentStartTime) / 1000.0));
    }

    private float getYPositionSteps() {
        return (float)(position.getY() + Math.signum(currentMovement.getY()) * Math.min(Math.abs(currentMovement.getY()), currentSpeed.getY() * (System.currentTimeMillis() - currentStartTime) / 1000.0));
    }

    public void abort() {
        lastSubmittedScript.cancel(true);
    }

    public interface ExecutionFinishedHandler {
        void commandExecutionFinished(List<Command> commands, ScriptRunner sender);
    }

    private class CommandWorkerRunnable implements Runnable {
        private List<Command> commands;
        private ExecutionFinishedHandler handler;

        public CommandWorkerRunnable(List<Command> commands, ExecutionFinishedHandler handler) {
            this.commands = commands;
            this.handler = handler;
        }

        @Override
        public void run() {
            try {
                for (Command current : commands) {
                    float timeNeededX = (current.getSteps().getX() != 0f ? (Math.abs(current.getSteps().getX()) * 1000f / current.getSpeed().getX()) : 0f);
                    float timeNeededY = (current.getSteps().getY() != 0f ? (Math.abs(current.getSteps().getY()) * 1000f / current.getSpeed().getY()) : 0f);

                    currentStartTime = System.currentTimeMillis();
                    currentMovement = current.getSteps();
                    currentSpeed = current.getSpeed();
                    control.sendCommand(current);
                    Thread.sleep((long) Math.max(timeNeededX, timeNeededY));
                    currentMovement = new Point2f(0, 0);
                    position = position.add(current.getSteps());
                }
                if(handler != null) {
                    handler.commandExecutionFinished(commands, ScriptRunner.this);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }


    }
}
