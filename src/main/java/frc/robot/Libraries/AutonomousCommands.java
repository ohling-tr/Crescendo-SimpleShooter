// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.Libraries;

/** Add your docs here. */
public enum AutonomousCommands {
    GENERIC_DEFAULT,
    BALANCE,
    BEND;


    public String getSelectName() {
        return this.toString();
    }

    public int getSelectIx() {
        return this.ordinal();
    }
}