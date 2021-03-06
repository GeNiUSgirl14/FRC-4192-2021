// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.hood;

import com.revrobotics.ControlType;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Constants.shooterModel;
import frc.robot.subsystems.Hood;

public class ResetHood extends CommandBase {
  /** Creates a new ResetHood. */
  private Hood m_hood;
  public ResetHood(Hood hood) {
    addRequirements(hood);
    m_hood = hood;
    // Use addRequirements() here to declare subsystem dependencies.
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    m_hood.getPIDController().setReference(shooterModel.resetPosition, ControlType.kPosition);
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {}

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    m_hood.getPIDController().setReference(0, ControlType.kVoltage);
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return Math.abs(m_hood.getPosition() - (shooterModel.resetPosition)) < shooterModel.tolerance;
  }
}
