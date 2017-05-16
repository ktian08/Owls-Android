package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class Joystick extends Touchpad {

    public Joystick(float deadzoneRadius, TouchpadStyle joystickStyle) {

        super(deadzoneRadius, joystickStyle);

    }

    public static TouchpadStyle returnTouchpadStyle(Texture jBackground, Texture jKnob) {

        Skin joystickSkin = new Skin();

        joystickSkin.add("touchBackground", jBackground);
        joystickSkin.add("touchKnob", jKnob);

        Touchpad.TouchpadStyle joystickStyle = new Touchpad.TouchpadStyle();

        Drawable touchBackground = joystickSkin.getDrawable("touchBackground");
        Drawable touchKnob = joystickSkin.getDrawable("touchKnob");

        joystickStyle.background = touchBackground;
        joystickStyle.knob = touchKnob;

        return joystickStyle;

    }


}
