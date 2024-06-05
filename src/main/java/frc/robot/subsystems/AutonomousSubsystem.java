package frc.robot.subsystems;

import java.util.Map;
import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.FunctionalCommand;
import edu.wpi.first.wpilibj2.command.PrintCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import edu.wpi.first.wpilibj2.command.button.RobotModeTriggers;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.RobotContainer;
import frc.robot.Libraries.ConsoleAuto;
//import frc.robot.Libraries.StepState;

//There is a 95% chance that it will crash if you try to run auto so dont
// Something interesting I found was DriverStation.getMatchTime() It returns how much time is left, might be useful.

public class AutonomousSubsystem extends SubsystemBase{

  public enum AutonomousCommands {
    WALLDRIVE,
    SPEAKERCENTER;

    public String getSelectName() {
        return this.toString();
    }

    public int getSelectIx() {
        return this.ordinal();
    }
  }

  public enum AutonomousSteps {
    WAIT1(),
    WAIT2(),
    WAITLOOP(),
    SHOOTNOTE(1),
    DRV_INTK_1(2),
    DRV_STRT_1(3,2),
    END()
    ;

    private final int m_iSwATrue;
    private final int m_iSwBFalse;

    private AutonomousSteps(int iSwATrue, int iSwBFalse) {
      this.m_iSwATrue = iSwATrue;
      this.m_iSwBFalse = iSwBFalse;
    }

    private AutonomousSteps(int iSwATrue) {
      this.m_iSwATrue = iSwATrue;
      this.m_iSwBFalse = 0;
    }

    private AutonomousSteps() {
      this.m_iSwATrue = 0;
      this.m_iSwBFalse = 0;
    }

    public int getASwitch() {
      return m_iSwATrue;
    }

    public int getBSwitch() {
      return m_iSwBFalse;
    }

    ;

  }

  private String kAUTO_TAB = "Autonomous";
  private String kSTATUS_PEND = "PEND";
  private String kSTATUS_ACTIVE = "ACTV";
  private String kSTATUS_DONE = "DONE";
  private String kSTATUS_SKIP = "SKIP";
  private String kSTATUS_NULL = "NULL";

  private int kSTEP_MAX = 8;

  ConsoleAuto m_ConsoleAuto;
  RobotContainer m_robotContainer;

  AutonomousCommands m_autoSelectCommand[] = AutonomousCommands.values();
  AutonomousCommands m_selectedCommand;

  private String m_strCommand;
  private String[] m_strStepList = new String[kSTEP_MAX];
  private String[] m_strStepSwitch = new String[kSTEP_MAX];
  private boolean[] m_bStepSWList = new boolean[kSTEP_MAX];
  private String[] m_strStepStatusList = new String[kSTEP_MAX];
  private int m_iCmdCount = 0;

  private ShuffleboardTab m_tab = Shuffleboard.getTab(kAUTO_TAB);

  private GenericEntry m_autoCmd = m_tab.add("Selected Pattern", "")
      .withPosition(2, 0)
      .withSize(2, 1)
      .getEntry();

  private GenericEntry m_iWaitLoop = m_tab.add("WaitLoop", 0)
      .withWidget(BuiltInWidgets.kDial)
      .withPosition(4, 0)
      .withSize(2, 2)
      .withProperties(Map.of("min", 0, "max", 5))
      .getEntry();

  private GenericEntry m_allianceColor = m_tab.add("Alliance", true)
      .withWidget(BuiltInWidgets.kBooleanBox)
      .withProperties(Map.of("colorWhenTrue", "Red", "colorWhenFalse", "Blue"))
      .withPosition(0, 0)
      .withSize(2, 2)
      .getEntry();

  private int m_iPatternSelect;

  //private Command m_currentCommand;
  //private boolean m_bIsCommandDone = false;
  //private int m_stepIndex;

  //private AutonomousSteps m_currentStepName;
  private AutonomousSteps[][] m_cmdSteps;

  public AutonomousSubsystem(ConsoleAuto consoleAuto, RobotContainer robotContainer) {

    m_ConsoleAuto = consoleAuto;
    m_robotContainer = robotContainer;
    m_selectedCommand = m_autoSelectCommand[0];
    m_strCommand = m_selectedCommand.toString();
    m_iPatternSelect = 0;

    for (int iat = 0; iat < kSTEP_MAX; iat++) {
      initStepList(iat);
      fmtDisplay(iat);
    }
  
/*
 *  CRITICAL PIECE
 * This two dimensional array defines the steps for each selectable Auto pattern
 * First dimension is set by the ConsoleAuto selector switch (passed in via POV 0)
 * Second dimension is the sequence of the step(s) for the pattern
 */
    m_cmdSteps = new AutonomousSteps[][] {
          {AutonomousSteps.WAITLOOP, AutonomousSteps.DRV_STRT_1},
          {AutonomousSteps.WAITLOOP, AutonomousSteps.SHOOTNOTE, AutonomousSteps.DRV_INTK_1, AutonomousSteps.DRV_STRT_1}
    };

  }

  private void fmtDisplay(int ix) {
  
    String labelName = "Step " + ix;
  
    m_tab
      .addString(labelName, () -> m_strStepList[ix])
      .withWidget(BuiltInWidgets.kTextView)
      .withSize(2,1)
      .withPosition(ix * 2, 3);

    labelName = "Switch(es) " + ix;
    m_tab
      .addString(labelName, () -> m_strStepSwitch[ix])
      .withPosition(ix *2, 4)
      .withSize(2, 1)
      .withWidget(BuiltInWidgets.kTextView);

    labelName = "SwState " + ix;
    m_tab
      .addBoolean(labelName, () -> m_bStepSWList[ix])
      .withPosition(ix *2, 5)
      .withSize(2, 1)
      .withWidget(BuiltInWidgets.kBooleanBox);
    
    labelName = "Status " + ix;
    m_tab
      .addString(labelName, () -> m_strStepStatusList[ix])
      .withPosition(ix *2, 6)
      .withSize(2, 1)
      .withWidget(BuiltInWidgets.kTextView);
  }

  private void initStepList(int ix) {
      m_strStepList[ix] = "";
      m_strStepSwitch[ix] = "";
      m_bStepSWList[ix] = false;
      m_strStepStatusList[ix] = "";
  }

    @Override
    public void periodic() {
    // This method will be called once per scheduler run
    }
   

  public void selectAutoCommand() {

    //System.out.println("in select auto method");

    int autoSelectIx = m_ConsoleAuto.getROT_SW_0();
    m_iPatternSelect = autoSelectIx;
    if (autoSelectIx >= m_cmdSteps.length) {
      autoSelectIx = 0;
      m_iPatternSelect = 0;
    }
    if (DriverStation.isDSAttached()) {
      //System.out.println(DriverStation.getAlliance().toString());
      boolean isAllianceRed = (DriverStation.getAlliance().get() == DriverStation.Alliance.Red);
      m_allianceColor.setBoolean(isAllianceRed);
    } else {
      m_allianceColor.setBoolean(true);
    }

    m_selectedCommand = m_autoSelectCommand[autoSelectIx];
    m_strCommand = m_selectedCommand.toString();
    m_autoCmd.setString(m_strCommand);

    int iWaitCount = m_ConsoleAuto.getROT_SW_1();
    m_iWaitLoop.setValue(iWaitCount);

    m_iCmdCount = 0;
    for (int ix = 0; ix < m_cmdSteps[autoSelectIx].length; ix++) {
      AutonomousSteps autoStep = m_cmdSteps[autoSelectIx][ix];
      m_strStepList[ix] = autoStep.name();
      m_strStepSwitch[ix] = getStepSwitch(autoStep);
      m_bStepSWList[ix] = getStepBoolean(autoStep);
      if (m_bStepSWList[ix]) {
        m_iCmdCount++;
      }
      m_strStepStatusList[ix] = kSTATUS_PEND;
    }
    for (int ix = m_cmdSteps[autoSelectIx].length; ix < kSTEP_MAX; ix++) {
      initStepList(ix);
    }

  }

  private String getStepSwitch(AutonomousSteps stepName) {
    String stepSwName = "";
    int stepSwitch = stepName.getASwitch();
    if (stepSwitch > 0) {
      stepSwName = String.valueOf(stepSwitch);
    }
    stepSwitch = stepName.getBSwitch();
    if (stepSwitch > 0) {
      stepSwName = stepSwName + " & !" + String.valueOf(stepSwitch);
    }
    return stepSwName;
  }
    
  private boolean getStepBoolean(AutonomousSteps stepName) {
    boolean stepBool = true;
    int stepSwitch = stepName.getASwitch();
    if (stepSwitch > 0) {
      stepBool = m_ConsoleAuto.getButton(stepSwitch);
    }
    stepSwitch = stepName.getBSwitch();
    if (stepSwitch > 0) {
      stepBool = stepBool & !m_ConsoleAuto.getButton(stepSwitch);
    }
    return stepBool;
  }
  
  public Command getWaitCommand(double seconds) {
    return Commands.waitSeconds(seconds);
  }

  /*
   * Command to run the Auto selection process with Operator Console interaction
   * This should be handled by a trigger that is started on Disabled status
   */
  public Command cmdAutoSelect() {
    //System.out.println("getting select auto command");
    return Commands.run(this::selectAutoCommand)
          .ignoringDisable(true);
  }

  /*
   * Command to process the selected command list
  */
  
  public Command cmdAutoControl() {

    Command autoCmdList[] = new Command[m_iCmdCount];

    int cmdIx = 0;
    for (int ix = 0; ix < m_cmdSteps[m_iPatternSelect].length; ix++) {
      if (m_bStepSWList[ix]) {
        autoCmdList[cmdIx] = Commands.print("command " + m_strStepList[ix] + cmdIx + " from " + ix);
        cmdIx++;
      }
    }

    SequentialCommandGroup autoCmd = new SequentialCommandGroup(autoCmdList);
    return autoCmd;
   
  }

  /*private void autoCntlInit() {
    m_stepIndex = -1;
    //private Command m_currentCommand;
    m_bIsCommandDone = true;
  }

  // this does not work as hoped
  // v2 coming soon
  private void autoCntlExecute() {
    m_currentStepName = null;
    System.out.println("in autoCntlExecute");

    if (m_bIsCommandDone) {
      m_bIsCommandDone = false;
      System.out.println("inside command trigger if");
      new Trigger(RobotModeTriggers.autonomous())
        .and(() -> true)
        .whileTrue(Commands.sequence(new PrintCommand("in triggered command")
                                ,new WaitCommand(4))
                .andThen(Commands.run(() -> isCommandDone())));
    }
  }

  public void isCommandDone() {
    m_bIsCommandDone = true;
  }
  

  private void autoCntlEnd(Boolean interrupted) {
    //m_currentCommand.end(interrupted);
  }

  private boolean autoCntlIsFinished() {
    boolean areWeThereYet = true;
    return areWeThereYet;
  }
  */
  
}
