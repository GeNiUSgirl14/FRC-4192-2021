/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.controller.PIDController;
import edu.wpi.first.wpilibj.controller.RamseteController;
import edu.wpi.first.wpilibj.controller.SimpleMotorFeedforward;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.trajectory.Trajectory;
import edu.wpi.first.wpilibj.trajectory.TrajectoryConfig;
import edu.wpi.first.wpilibj.trajectory.TrajectoryGenerator;
import edu.wpi.first.wpilibj.trajectory.TrajectoryUtil;
import edu.wpi.first.wpilibj.trajectory.constraint.DifferentialDriveVoltageConstraint;
import frc.robot.Constants.drivePID;
import frc.robot.Constants.visionPosition.blueA;
import frc.robot.Constants.visionPosition.blueB;
import frc.robot.Constants.visionPosition.redA;
import frc.robot.Constants.visionPosition.redB;
import frc.robot.commands.turret.AlignWithTarget;
import frc.robot.commands.drive.DefaultDrive;
import frc.robot.commands.autonomous.DistanceAuton;
import frc.robot.commands.autonomous.RedSearchAutonA;
import frc.robot.commands.autonomous.RedSearchAutonB;
import frc.robot.commands.drive.DriveForDistance;
import frc.robot.commands.drive.DriveStraight;
import frc.robot.commands.intake.IntakeBalls;
import frc.robot.commands.intake.OuttakeSlowly;
import frc.robot.commands.drive.PrecisionDrive;
import frc.robot.commands.hood.ResetHood;
import frc.robot.commands.hood.RunHood;
import frc.robot.commands.index.IndexIn;
import frc.robot.commands.index.IndexOut;
import frc.robot.commands.turret.TurretTurn;
import frc.robot.commands.vision.VisionDefault;
import frc.robot.commands.macros.UnjamBall;
import frc.robot.commands.shootingSystem.RunShooter;
import frc.robot.commands.macros.ShootingMacro;
import frc.robot.commands.autonomous.BasicAuton;
import frc.robot.commands.autonomous.BlueSearchAutonA;
import frc.robot.commands.autonomous.BlueSearchAutonB;
import frc.robot.commands.testingSystem.TestMotor;
import frc.robot.commands.macros.TurretAlignmentMacro;
import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.Hood;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.ShootingSystem;
import frc.robot.subsystems.Index;
import frc.robot.subsystems.Turret;
import frc.robot.subsystems.Vision;
import frc.robot.subsystems.TestingSystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.RamseteCommand;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.Trigger;

/**
 * This class is where the bulk of the robot should be declared. Since
 * Command-based is a "declarative" paradigm, very little robot logic should
 * actually be handled in the {@link Robot} periodic methods (other than the
 * scheduler calls). Instead, the structure of the robot (including subsystems,
 * commands, and button mappings) should be declared here.
 */
public class RobotContainer {
 
  private final DoubleSupplier centerX;
  
  // Controllers
  private final XboxController driveController = new XboxController(Constants.driveController);
  private final XboxController systemsController = new XboxController(Constants.systemsController);

  // Subsystems
  private final Drivetrain m_drive = new Drivetrain();
  private final Intake m_intake = new Intake();
  private final ShootingSystem m_ShootingSystem = new ShootingSystem();
  private final Index m_Index = new Index();
  private final Turret m_Turret = new Turret();
  private final TestingSystem m_motor = new TestingSystem();
  private final Hood m_Hood = new Hood();
  private final Vision m_vision = new Vision();

  // Commands
  private final DefaultDrive m_driveCommand = new DefaultDrive(m_drive, () -> driveController.getY(Hand.kLeft),
      () -> driveController.getX(Hand.kRight));
  private final PrecisionDrive m_halfSpeedDrive = new PrecisionDrive(m_drive, () -> driveController.getY(Hand.kLeft),
      () -> driveController.getX(Hand.kRight), 0.5);
  private final PrecisionDrive m_quarterSpeedDrive = new PrecisionDrive(m_drive, () -> driveController.getY(Hand.kLeft),
      () -> driveController.getX(Hand.kRight), 0.3);
  private final DriveStraight m_driveStraight = new DriveStraight(m_drive, () -> driveController.getY(Hand.kLeft));
  private final IntakeBalls m_intakeCommand = new IntakeBalls(m_intake, Constants.intakeSpeed);
  private final OuttakeSlowly m_outtakeSlowlyCommand = new OuttakeSlowly(m_intake, Constants.outtakeSlowlySpeed);
  private final IndexIn m_indexInCommand = new IndexIn(m_Index, Constants.indexSpeed);
  private final IndexOut m_indexOutCommand = new IndexOut(m_Index, Constants.indexSpeed);
  private final TurretTurn m_turretTurnLeft = new TurretTurn(m_Turret,
      () -> systemsController.getTriggerAxis(Hand.kLeft) * 7 / 10);
  private final TurretTurn m_turretTurnRight = new TurretTurn(m_Turret,
      () -> -systemsController.getTriggerAxis(Hand.kRight) * 7 / 10);
  private final RunShooter m_runShooter = new RunShooter(m_ShootingSystem);
  private final UnjamBall m_unjamBalls = new UnjamBall(m_Index, m_ShootingSystem, Constants.unjamBalls.ind_power,
      Constants.unjamBalls.s_power, Constants.unjamBalls.f_power);
  private final RunHood m_runHoodForward = new RunHood(m_Hood, 0.2);
  private final RunHood m_runHoodBackward = new RunHood(m_Hood, -0.2);
  private final ResetHood m_resetHood = new ResetHood(m_Hood);
  private final VisionDefault m_visionDefault = new VisionDefault(m_vision);
  // private final AlignWithTarget m_alignWithTarget = new
  // AlignWithTarget(m_Turret);
  private final TurretAlignmentMacro m_turretMacro = new TurretAlignmentMacro(m_drive, m_Turret, m_Hood);
  private final ShootingMacro m_shooterMacro = new ShootingMacro(m_drive, m_Turret, m_ShootingSystem, m_Index, m_Hood);
  private final TestMotor m_testMotor = new TestMotor(m_motor, 0.3);

  // Autonomous Commands
  private final BasicAuton m_basicauton = new BasicAuton(m_drive);
  private final BlueSearchAutonA autonBlueA = new BlueSearchAutonA(m_drive, m_intake);
  private final BlueSearchAutonB autonBlueB = new BlueSearchAutonB(m_drive, m_intake);
  private final RedSearchAutonA autonRedA = new RedSearchAutonA(m_drive, m_intake);
  private final RedSearchAutonB autonRedB = new RedSearchAutonB(m_drive, m_intake);
  private final DriveForDistance zeroDistance = new DriveForDistance(m_drive, 0);
  // private final DistanceAuton m_distanceauton = new DistanceAuton(m_drive);
  private final DistanceAuton m_distanceauton = new DistanceAuton(m_drive, m_Turret, m_ShootingSystem, m_Index, m_Hood);

  // Triggers
  Trigger rightTrigger = new Trigger(() -> driveController.getTriggerAxis(Hand.kRight) > 0.6);
  Trigger leftTrigger = new Trigger(() -> driveController.getTriggerAxis(Hand.kLeft) > 0.6);
  JoystickButton leftBumper = new JoystickButton(driveController, Constants.leftBumper);
  JoystickButton systemsStartButton = new JoystickButton(systemsController, Constants.startButton);
  JoystickButton systemsBackButton = new JoystickButton(systemsController, Constants.backButton);
  JoystickButton systemsXButton = new JoystickButton(systemsController, Constants.xButton);
  JoystickButton systemsAButton = new JoystickButton(systemsController, Constants.aButton);
  JoystickButton aButton = new JoystickButton(driveController, Constants.aButton);
  JoystickButton systemsYButton = new JoystickButton(systemsController, Constants.yButton);
  JoystickButton driverBackButton = new JoystickButton(driveController, Constants.backButton);
  JoystickButton driverYButton = new JoystickButton(driveController, Constants.yButton);
  JoystickButton driverStartButton = new JoystickButton(driveController, Constants.startButton);
  Trigger rightTriggerSubsystems = new Trigger(() -> systemsController.getTriggerAxis(Hand.kRight) > 0.2);
  Trigger leftTriggerSubsystems = new Trigger(() -> systemsController.getTriggerAxis(Hand.kLeft) > 0.2);
  Trigger joystickYOnly = new Trigger(
      () -> Math.abs(driveController.getX(Hand.kRight)) < 0.05 && Math.abs(driveController.getY(Hand.kLeft)) > 0.05
          && driveController.getTriggerAxis(Hand.kRight) < 0.6 && driveController.getTriggerAxis(Hand.kLeft) < 0.6);

  public RobotContainer(DoubleSupplier maxCenterX) {
    m_drive.setDefaultCommand(m_driveCommand);
    centerX = maxCenterX;
    // Configure the button bindings
    configureButtonBindings();
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be
   * created by instantiating a {@link GenericHID} or one of its subclasses
   * ({@link edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then
   * passing it to a {@link edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureButtonBindings() {
    rightTrigger.whileActiveOnce(m_halfSpeedDrive);
    leftTrigger.whileActiveOnce(m_quarterSpeedDrive);
    rightTriggerSubsystems.whileActiveOnce(m_turretTurnRight);
    leftTriggerSubsystems.whileActiveOnce(m_turretTurnLeft);
    systemsStartButton.whenHeld(m_indexInCommand);
    // systemsBackButton.whenHeld(m_indexOutCommand);
    systemsBackButton.whenPressed(m_resetHood);
    leftBumper.toggleWhenPressed(m_intakeCommand);
    aButton.whenHeld(m_outtakeSlowlyCommand);
    systemsYButton.toggleWhenPressed(m_shooterMacro);
    driverBackButton.whenHeld(m_unjamBalls);
    joystickYOnly.whileActiveOnce(m_driveStraight, false);
    // joystickYOnly.whileActiveOnce(m_driveStraight);
    driverStartButton.whenHeld(m_testMotor);
    systemsXButton.whenHeld(m_runHoodForward);
    systemsAButton.whenHeld(m_runHoodBackward);
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    if (centerX.getAsDouble() > blueA.left && centerX.getAsDouble() < blueA.right) {
      return autonBlueA;
    }

    else if (centerX.getAsDouble() > blueB.left && centerX.getAsDouble() < blueB.right) {
      return autonBlueB;
    }

    else if (centerX.getAsDouble() > redA.left && centerX.getAsDouble() < redA.right) {
      return autonRedA;
    }

    else if (centerX.getAsDouble() > redB.left && centerX.getAsDouble() < redB.right) {
      return autonRedB;
    }

    else {
      return zeroDistance;
    }

    /*
     * Path A
     * 
     * if(red object present == true) { run RedSearchAutonA(); } else { run
     * BlueSearchAutonA(); }
     */
  }
}
