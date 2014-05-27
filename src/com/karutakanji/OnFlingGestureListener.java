package com.karutakanji;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

public abstract class OnFlingGestureListener implements OnTouchListener 
{

	  private final GestureDetector gdt = new GestureDetector(new GestureListener());
	  private MyImageView viewAssociada;
	  private Activity activityAssociada;
	  
	  @Override
	  public boolean onTouch(final View v, final MotionEvent event) {
	     gdt.onTouchEvent(event);
	     return true;
	  }

	  private final class GestureListener extends SimpleOnGestureListener {

	     //private static final int SWIPE_MIN_DISTANCE = 60;
		  private static final int SWIPE_MIN_DISTANCE = 0;
	     //private static final int SWIPE_THRESHOLD_VELOCITY = 100;
		  private static final int SWIPE_THRESHOLD_VELOCITY = 50;
		  
	     @Override
	     public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
	        if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	           onRightToLeft();
	           return true;
	        } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	           onLeftToRight();
	           return true;
	        }
	        if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
	           onBottomToTop();
	           return true;
	        } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
	           onTopToBottom();
	           return true;
	        }
	        return false;
	     }
	  }


	public MyImageView getViewAssociada() {
		return viewAssociada;
	}

	public void setViewAssociada(MyImageView viewAssociada) {
		this.viewAssociada = viewAssociada;
	}
	
	public Activity getActivityAssociada() {
		return activityAssociada;
	}

	public void setActivityAssociada(Activity activityAssociada) {
		this.activityAssociada = activityAssociada;
	}
	
	public void onTopToBottom() 
    {
		Timer t = new Timer();
		  t.scheduleAtFixedRate(new TimerTask() 
			{

			    @Override
			    public void run() 
			    {
			    	activityAssociada.runOnUiThread(new Runnable() {

			    	    @Override
			    	    public void run() 
			    	    {
			    	    	float dx = 0f;
		    	    		float dy = +20f;
			    	    	viewAssociada.changePos(dx, dy);
			    	    }
			    	     
			    	});
			    }
			         
			},
			//Set how long before to start calling the TimerTask (in milliseconds)
			0,
			//Set the amount of time between each execution (in milliseconds)
			500);
	}

    
    public void onRightToLeft() 
    {
    	Timer t = new Timer();
		  t.scheduleAtFixedRate(new TimerTask() 
			{

			    @Override
			    public void run() 
			    {
			    	activityAssociada.runOnUiThread(new Runnable() {

			    	    @Override
			    	    public void run() 
			    	    {
			    	    	float dx = -20f;
		    	    		float dy = 0f;
			    	    	viewAssociada.changePos(dx, dy);
			    	    }
			    	     
			    	});
			    }
			         
			},
			//Set how long before to start calling the TimerTask (in milliseconds)
			0,
			//Set the amount of time between each execution (in milliseconds)
			500);
    }

    
    public void onLeftToRight() 
    {
    	Timer t = new Timer();
		  t.scheduleAtFixedRate(new TimerTask() 
			{

			    @Override
			    public void run() 
			    {
			    	activityAssociada.runOnUiThread(new Runnable() {

			    	    @Override
			    	    public void run() 
			    	    {
			    	    	float dx = +20f;
		    	    		float dy = 0f;
			    	    	viewAssociada.changePos(dx, dy);
			    	    }
			    	     
			    	});
			    }
			         
			},
			//Set how long before to start calling the TimerTask (in milliseconds)
			0,
			//Set the amount of time between each execution (in milliseconds)
			500);
    }

    
    public void onBottomToTop() 
    {
    	Timer t = new Timer();
		  t.scheduleAtFixedRate(new TimerTask() 
			{

			    @Override
			    public void run() 
			    {
			    	activityAssociada.runOnUiThread(new Runnable() {

			    	    @Override
			    	    public void run() 
			    	    {
			    	    	float dx = 0f;
		    	    		float dy = -20f;
			    	    	viewAssociada.changePos(dx, dy);
			    	    }
			    	     
			    	});
			    }
			         
			},
			//Set how long before to start calling the TimerTask (in milliseconds)
			0,
			//Set the amount of time between each execution (in milliseconds)
			500);
    }

	
}

