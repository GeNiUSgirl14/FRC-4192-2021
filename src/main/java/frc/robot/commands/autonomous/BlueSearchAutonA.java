// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.autonomous;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.Intake;
import frc.robot.Constants.drivePID;
import frc.robot.commands.drive.DriveForDistance;
import frc.robot.commands.drive.RotateInPlace;

// NOTE:  Consider using this command inline, rather than writing a subclass.  For more
// information, see:
// https://docs.wpilib.org/en/stable/docs/software/commandbased/convenience-features.html
public class BlueSearchAutonA extends SequentialCommandGroup {
  /** Creates a new BlueSearchAutonA. */
  public BlueSearchAutonA(Drivetrain m_drive, Intake m_intake) {
    // Add your commands in the addCommands() call, e.g.
    // addCommands(new FooCommand(), new BarCommand());
    addCommands(new RotateInPlace(m_drive, 21.80),
        new DriveAndIntake(m_drive, m_intake, 15.8 * drivePID.feetToRotations), new RotateInPlace(m_drive, -93.37),
        new DriveAndIntake(m_drive, m_intake, 7.9 * drivePID.feetToRotations), new RotateInPlace(m_drive, 98.14),
        new DriveAndIntake(m_drive, m_intake, 5.6 * drivePID.feetToRotations), new RotateInPlace(m_drive, -26.6),
        new DriveAndIntake(m_drive, m_intake, 5 * drivePID.feetToRotations));
    // Change zeroes to correct distances
  }
}
