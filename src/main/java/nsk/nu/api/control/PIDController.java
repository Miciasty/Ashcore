package nsk.nu.api.control;

/** Simple PID for scalar targets (dt in seconds). */
public final class PIDController {
    private final double kp, ki, kd;
    private double integral = 0, prevError = 0;
    private boolean first = true;

    public PIDController(double kp, double ki, double kd){ this.kp=kp; this.ki=ki; this.kd=kd; }

    /** @return control output; dt must be > 0 */
    public double update(double target, double current, double dt){
        double e = target - current;
        integral += e * dt;
        double derivative = first ? 0 : (e - prevError) / dt;
        first = false; prevError = e;
        return kp*e + ki*integral + kd*derivative;
    }
}