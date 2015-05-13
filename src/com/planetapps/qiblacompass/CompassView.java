package com.planetapps.qiblacompass;

import java.util.concurrent.atomic.AtomicBoolean;

import com.planetapps.qiblacompass.data.GlobalData;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


/**
 * This class extends the View class and is designed draw the compass on the View.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class CompassView extends View {
	private static final AtomicBoolean drawing = new AtomicBoolean(false);
	private static final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	private static int parentWidth = 0;
	private static int parentHeight = 0;
	
	private static Matrix matrix = null;
    private static Bitmap bitmap = null;

    public CompassView(Context context) {
        super(context);
        
        initialize();
    }    
    
    public CompassView(Context context, AttributeSet attr) {
        super(context,attr);
        
        initialize();
    }
    
    public static Bitmap getBitmap() {
		return bitmap;
	}

	public static void setBitmap(Bitmap bitmap) {
		CompassView.bitmap = bitmap;
	}

	private void initialize() {
		
		System.out.println("getDialCode >"+GlobalData.getDialCode());
        matrix = new Matrix();
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.compass_icon);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(parentWidth, parentHeight);
    }
    
    @Override
    protected void drawableStateChanged () {
    	System.out.println("in drawableStateChanged >"+GlobalData.getDialCode());
    	
    	if(GlobalData.getDialCode() == 1)
    	{
    		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dial1);
    	}else if (GlobalData.getDialCode() == 2)
    	{
    		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dial2);
    	}else if (GlobalData.getDialCode() == 3)
    	{
    		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dial3);
    	}else if (GlobalData.getDialCode() == 4)
    	{
    		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dial4);
    	}else if (GlobalData.getDialCode() == 5)
    	{
    		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dial5);
    	}else if (GlobalData.getDialCode() == 6)
    	{
    		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dial6);
    	}else if (GlobalData.getDialCode() == 7)
    	{
    		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dial7);
    	}else if (GlobalData.getDialCode() == 8)
    	{
    		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dial8);
    	}else if (GlobalData.getDialCode() == 9)
    	{
    		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dial9);
    	}
    }    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDraw(Canvas canvas) {
    	if (canvas==null) throw new NullPointerException();

        if (!drawing.compareAndSet(false, true)) return; 

        float bearing = GlobalData.getBearing();

        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        if (bitmap.getWidth()>canvasWidth || bitmap.getHeight()>canvasHeight) {        
            //Resize the bitmap to the size of the canvas
            bitmap = Bitmap.createScaledBitmap(bitmap, (int)(bitmapWidth*.9), (int)(bitmapHeight*.9), true);
        }
        
        int bitmapX = bitmap.getWidth()/2;
        int bitmapY = bitmap.getHeight()/2;
        
        int parentX = parentWidth/2;
        int parentY = parentHeight/2;
        
        int centerX = parentX-bitmapX;
        int centerY = parentY-bitmapY;
        
        int rotation = (int)(360-bearing);
        
        matrix.reset();
        //Rotate the bitmap around it's center point so it's always pointing north
        matrix.setRotate(rotation, bitmapX, bitmapY);
        //Move the bitmap to the center of the canvas
        matrix.postTranslate(centerX, centerY);

        canvas.drawBitmap(bitmap, matrix, paint);

	    drawing.set(false);
    }
}
