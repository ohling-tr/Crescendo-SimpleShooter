// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

/**
 * The Constants class provides a convenient place for teams to hold robot-wide numerical or boolean
 * constants. This class should not be used for any other purpose. All constants should be declared
 * globally (i.e. public static). Do not put anything functional in this class.
 *
 * <p>It is advised to statically import this class (or one of its inner classes) wherever the
 * constants are needed, to reduce verbosity.
 */
public final class Constants {

  public static final class CANIDConstants {

    /*
    public static final int kFrontLeftDrivingCanId = 4;
    public static final int kRearLeftDrivingCanId = 8;
    public static final int kFrontRightDrivingCanId = 2;
    public static final int kRearRightDrivingCanId = 6;

    public static final int kFrontLeftTurningCanId = 5;
    public static final int kRearLeftTurningCanId = 9;
    public static final int kFrontRightTurningCanId = 3;
    public static final int kRearRightTurningCanId = 7;
    */

    public static final int kSHOOTER_LEFT_MOTOR_ID = 10;
    public static final int kSHOOTER_RIGHT_MOTOR_ID = 11;

    public static final int kINTAKE_LIFT_MOTOR_ID = 20;
    public static final int kINTAKE_SPIN_MOTOR_ID = 21;
    
    //public static final int kCLIMBER_LEFT_MOTOR_ID = 30;
    //public static final int kCLIMBER_RIGHT_MOTOR_ID = 31;

  } 

  public static class OperatorConstants {

    public static final int kDRIVER_CONTROLLER_PORT = 0;

    public static final double kCONTROLLER_TRIGGER_THRESHOLD = 0.75;
    public static final double kINTAKE_FEED_DELAY = 0.8;

  }

  public static class ShooterConstants {

    public static final boolean kIS_SHOOTER_INVERTED = false;
    public static final int kSHOOTER_CURRENT_LIMIT = 40;

    public static final double kSHOOT_SPEED_IDLE = 0;
    public static final double kSHOOT_SPEED_LAUNCH = 0.9;

  }

  public static class IntakeConstants {
    public static final double kSPINNER_SPEED_EJECT = -0.9;
    public static final double kSPINNER_SPEED_INTAKE = 1.0;
    public static final double kSPINNER_SPEED_IDLE = 0;
  }

}
