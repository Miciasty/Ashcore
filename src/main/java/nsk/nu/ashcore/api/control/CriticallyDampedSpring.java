package nsk.nu.ashcore.api.control;

/** Critically damped spring (2nd order) for smooth value tracking without overshoot. */
public final class CriticallyDampedSpring {
    private double y, v;
    private final double omega; // natural frequency

    public CriticallyDampedSpring(double initial, double frequencyHz){
        this.y = initial; this.v = 0; this.omega = 2*Math.PI*frequencyHz;
    }
    /** Advances towards target by dt seconds; returns new value. */
    public double update(double target, double dt){
        double f = omega;
        double x = y - target;
        double exp = Math.exp(-f*dt);
        y = target + (x*(1 + f*dt) + v*dt) * exp;
        v = (v - f*x*(f*dt)) * exp;
        return y;
    }
}
