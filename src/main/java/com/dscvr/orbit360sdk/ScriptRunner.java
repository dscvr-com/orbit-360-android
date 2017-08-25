package com.dscvr.orbit360sdk;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This class allows asynchronous execution of multiple commands, in order. It also provides basic
 * estimation of the position of the Orbit360's arm.
 *
 * If you use this class, you should not call methods of the passed Orbit360Control instance directly.
 * Also, you should not use two ScriptRunner instances on the same Orbit360Control instance simultaneously.
 *
 * The position estimation is always relative to the initial position, when the ScriptRunner instance
 * was created.
 */
public class ScriptRunner {

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<?> lastSubmittedScript = null;
    private Orbit360Control control;
    private long currentStartTime;
    private Point2f currentMovement;
    private Point2f currentSpeed;
    private Point2f position;

    /**
     * Creates a new instance of this class.
     * @param control The connected Orbit360Control instance.
     */
    public ScriptRunner(Orbit360Control control) {
        this.control = control;
        this.position = new Point2f(0, 0);
        this.currentSpeed = new Point2f(0, 0);
        this.currentMovement = new Point2f(0, 0);
    }

    /**
     * Executes a list of commands, in order. If this method is called while another execution is
     * still in progress, it will raise an exception.
     * @param commands The commands to execute.
     * @param handler The callback to call when execution is finished.
     */
    public void runScript(List<Command> commands, ExecutionFinishedHandler handler) {
        if (lastSubmittedScript == null || lastSubmittedScript.isDone())
            lastSubmittedScript = executor.submit(new CommandWorkerRunnable(commands, handler));
        else{
            throw new IllegalStateException("there is already a circle Command Running");
        }
    }

    /**
     * @return The estimated position of the arm in steps.
     */
    public Point2f getPositionSteps() {
        return new Point2f(getXPositionSteps(), getYPositionSteps());
    }

    /**
     * @return The estimated position of the arm in degrees.
     */
    public Point2f getPosition() {
        return new Point2f(getXPositionSteps(), getYPositionSteps()).div(Orbit360Control.DEGREES_TO_STEPS);
    }

    private float getXPositionSteps() {
        return (float)(position.getX() + Math.signum(currentMovement.getX()) * Math.min(Math.abs(currentMovement.getX()), currentSpeed.getX() * (System.currentTimeMillis() - currentStartTime) / 1000.0));
    }

    private float getYPositionSteps() {
        return (float)(position.getY() + Math.signum(currentMovement.getY()) * Math.min(Math.abs(currentMovement.getY()), currentSpeed.getY() * (System.currentTimeMillis() - currentStartTime) / 1000.0));
    }

    /**
     * Cancels execution after the current command in the queue has finished.
     * The estimated position will still be consistent.
     */
    public void abort() {
        lastSubmittedScript.cancel(true);
    }

    /**
     * Interface for the callback which is called upon finished execution.
     */
    public interface ExecutionFinishedHandler {
        /**
         * @param commands The list of command which was executed.
         * @param sender The script runner which invoked the callback.
         */
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
