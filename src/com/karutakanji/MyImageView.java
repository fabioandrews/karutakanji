package com.karutakanji;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;


public class MyImageView extends ImageView 
{

private float mPosX;
private float mPosY;
private float mScaleFactor = 1.f;
private boolean UsuarioEstaMechendoAView = false; //diz se o cursor do usuario esta mechendo a view ou nao

public boolean getUsuarioEstaMechendoAView() {
	return UsuarioEstaMechendoAView;
}
public void setUsuarioEstaMechendoAView(boolean usuarioEstaMechendoAView) {
	UsuarioEstaMechendoAView = usuarioEstaMechendoAView;
}
/**
 * Return a new image view.
 */

public MyImageView (Context context) {
	super (context);
}
public MyImageView (Context context, AttributeSet attrs) {
	super (context, attrs);
}
public MyImageView (Context context, AttributeSet attrs, int style) {
	super (context, attrs, style);
}

/**
 * Draw the image on the canvas, after translating the canvas position by mPosX and mPosY.
 * 
 * @param canvas Canvas
 * @return void
 */

public void onDraw(Canvas canvas) 
{
    canvas.translate(mPosX, mPosY);
    super.onDraw(canvas);
    
    
}

/**
 * Take the current x and y values used to translate the canvas and change them by dx and dy.
 * Also calls invalidate so the view will be redrawn.
 * 
 * @param dx float change in x position
 * @param dy float change in y
 * @return void
 */

public void changePos (float dx, float dy)
{
    mPosX += dx;
    mPosY += dy;
    this.invalidate ();
} // end changePos

/**
 * Sets the x and y values used to translate the canvas.
 * Also calls invalidate so the view will be redrawn.
 * 
 * @param x float
 * @param y float
 * @return void
 */

public void setPos (float x, float y)
{
    mPosX = x;
    mPosY = y;
    this.invalidate ();
} // end setPos



} // end class

