// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import frc.robot.Constants.OperatorConstants;
import frc.robot.Libraries.ConsoleAuto;
import frc.robot.subsystems.AutonomousSubsystem;
import frc.robot.subsystems.DriveSubsystem;
import frc.robot.subsystems.NoteIntakeSubsystem;
import frc.robot.subsystems.NoteShooterSubsystem;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.RobotModeTriggers;
import edu.wpi.first.wpilibj2.command.button.Trigger;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and trigger mappings) should be declared here.
 */
public class RobotContainer {

  private final NoteShooterSubsystem m_noteShooterSubsystem = new NoteShooterSubsystem();
  private final NoteIntakeSubsystem m_noteIntakeSubsystem = new NoteIntakeSubsystem();
  private final DriveSubsystem m_driveSubsystem = new DriveSubsystem();

  private final CommandXboxController m_driverController =
      new CommandXboxController(OperatorConstants.kDRIVER_CONTROLLER_PORT);

  private final ConsoleAuto m_consoleAuto =
      new ConsoleAuto(OperatorConstants.kCONSOLE_AUTO_PORT);

  private final AutonomousSubsystem m_autonomousSubysystem = new AutonomousSubsystem(m_consoleAuto, this);

  static boolean m_runAutoConsole;

  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {

    m_noteShooterSubsystem.setDefaultCommand(m_noteShooterSubsystem.cmdShooterIdle());
    m_noteIntakeSubsystem.setDefaultCommand(m_noteIntakeSubsystem.cmdSpinnerIdle());

    // Configure the trigger bindings
    configureBindings();
  }

  /**
   * Use this method to define your trigger->command mappings. Triggers can be created via the
   * {@link Trigger#Trigger(java.util.function.BooleanSupplier)} constructor with an arbitrary
   * predicate, or via the named factories in {@link
   * edu.wpi.first.wpilibj2.command.button.CommandGenericHID}'s subclasses for {@link
   * CommandXboxController Xbox}/{@link edu.wpi.first.wpilibj2.command.button.CommandPS4Controller
   * PS4} controllers or {@link edu.wpi.first.wpilibj2.command.button.CommandJoystick Flight
   * joysticks}.
   */
  private void configureBindings() {
    // Schedule `ExampleCommand` when `exampleCondition` changes to `true`
    //new Trigger(m_exampleSubsystem::exampleCondition)
    //    .onTrue(new ExampleCommand(m_exampleSubsystem));

    runAutoConsoleFalse();
    //new Trigger(DriverStation::isDisabled)
    //new Trigger(RobotModeTriggers.disabled())
    new Trigger(trgAutoSelect())
      .whileTrue(m_autonomousSubysystem.cmdAutoSelect());
    runAutoConsoleTrue();

    //THIS DOES NOT WORK //
    new Trigger(RobotModeTriggers.disabled())
      .onTrue(Commands.runOnce(this::runAutoConsoleTrue))
      ;
    // WHY NOT??????

    new Trigger(RobotModeTriggers.disabled())
      .onFalse(Commands.runOnce(this::runAutoConsoleFalse))
      ;

    m_driverController.leftTrigger(OperatorConstants.kCONTROLLER_TRIGGER_THRESHOLD)
      .whileTrue(cmdShootNote());

  }

  private static Trigger trgAutoSelect() {
    //System.out.println("bool auto console" + m_runAutoConsole);
    return new Trigger(() -> m_runAutoConsole);
  }

  private void runAutoConsoleTrue() {
    m_runAutoConsole = true;
    System.out.println("true " + m_runAutoConsole);
  }

  private void runAutoConsoleFalse() {
    m_runAutoConsole = false;
    System.out.println("false " + m_runAutoConsole);
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    // An example command will be run in autonomous
    return m_autonomousSubysystem.cmdAutoControl();

  }

  /*
   * Command to shoot the NOTE
   * Parallel command to spin up the Shooter
   * with Wait period to allow shooter to come to speed (could be a wait until on shooter RPM true)
   * followed by Intake feeding out the NOTE
   * 
   * Available as public so it can be used by Autonomous
   */
  public Command cmdShootNote() {
    return (Commands.parallel(m_noteShooterSubsystem.cmdShooterLaunch(),
                Commands.sequence(Commands.waitSeconds(OperatorConstants.kINTAKE_FEED_DELAY),
                    m_noteIntakeSubsystem.cmdSpinnerEject()))
           );
  }

  public Command getDrivePath() {
    return m_driveSubsystem.getDrivePath();
  }

}
