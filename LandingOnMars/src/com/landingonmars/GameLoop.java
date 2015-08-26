package com.landingonmars;

import com.jcasey.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameLoop extends SurfaceView implements Runnable,
		SurfaceHolder.Callback
{


	public static final double INITIAL_TIME = 2.5;
	static final int REFRESH_RATE = 20;
	static final int GRAVITY = 1;
	static final int FUEL_BOOST = 3;

	Thread main;
	SoundPool soundPool;
	
	
	int xcor[] = { 0, 200, 190, 218, 260, 275, 300, 309, 327, 336, 368, 382,
			448, 462, 476, 498, 527, 600, 600, 0, 0 };
	int ycor[] = { 626, 540, 550, 670, 670, 594, 530, 520, 520, 527, 626, 636,
			636, 623, 535, 504, 481, 481, 750, 750, 616 };
	
	private int thruster;
	private int finish;
	private int explosion;
	
	private Bitmap background;
	private Bitmap leftThruster;
	private Bitmap explosion2;	
	private Bitmap rightThruster;
	private Bitmap apollo;
	private int fuelBoost = 0;
	private Bitmap mainThruster;
	private int fuelLevel = 100;
	private Bitmap mars;

	Paint landingSurface = new Paint();
	Paint paint = new Paint();
	Paint fuel = new Paint();
	Paint bar = new Paint();
	






	Canvas offscreen;
	Bitmap buffer;

	
	Path pathway;
	Path landing;
	
	boolean downPressed = false;
	boolean leftPressed = false;
	boolean rightPressed = false;
	boolean gameover = false;

	float x, y;
	int width = 0;

	double t = INITIAL_TIME;

	
	

	int landX[] = { 385, 460, 460, 385, 385 };
	int landY[] = { 626, 626, 636, 636, 626 };

	public GameLoop(Context context) {
		super(context);

		init();
	}

	public GameLoop(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init();
	}

	public GameLoop(Context context, AttributeSet attrs) {
		super(context, attrs);

		init();
	}

	public void init() {

		soundPool = new SoundPool(5, AudioManager.STREAM_SYSTEM, 5);
		explosion = soundPool.load(getContext(), R.raw.explosion2, 1);
		thruster = soundPool.load(getContext(), R.raw.thruster2, 2);
		finish = soundPool.load(getContext(), R.raw.endgame, 3);

		pathway = new Path();

		for (int i = 0; i < xcor.length; i++) {
			pathway.lineTo(xcor[i], ycor[i]);
		}

		getHolder().addCallback(this);
		apollo = BitmapFactory.decodeResource(getResources(), R.drawable.craftsmain);
		explosion2 = BitmapFactory.decodeResource(getResources(), R.drawable.collision);
		background = BitmapFactory.decodeResource(getResources(),
				R.drawable.surface);
		leftThruster = BitmapFactory.decodeResource(getResources(),
				R.drawable.thruster);
		rightThruster = BitmapFactory.decodeResource(getResources(),
				R.drawable.thruster);
		mainThruster = BitmapFactory.decodeResource(getResources(),
				R.drawable.thrust);
		mars = BitmapFactory.decodeResource(getResources(), R.drawable.mars);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		width = w;

		x = width / 2;
	}

	public void run() {
		while (true) {
			while (!gameover) {
				Canvas canvas = null;
				SurfaceHolder holder = getHolder();
				synchronized (holder) {
					canvas = holder.lockCanvas();
					canvas.drawColor(Color.BLACK);
					paint.setColor(Color.GREEN);

					Shader mShader = new BitmapShader(mars,
							Shader.TileMode.REPEAT, Shader.TileMode.MIRROR);
					paint.setShader(mShader);
					canvas.drawPath(pathway, paint);
					fuellevel(canvas);
					landing(canvas);

					
					y = (int) y + (int) ((0.5 * ((GRAVITY - fuelBoost) * t * t)));

					t = t + 0.01; 

					if (x < 0) {
						x = x + getWidth();
					} else if (x > getWidth()) {
						x = x - getWidth();

					}

					boolean landLeft = contains(landX, landY, x - 28,
							y + 25);
					boolean landRight = contains(landX, landY, x + 25,
							y + 25);
					boolean bottomLeft = contains(xcor, ycor, x - 25, y + 25);
					boolean bottomRight = contains(xcor, ycor, x + 25, y + 25);

					if (landLeft && landRight) {
						soundPool.play(finish, 1, 3, 3, 0, 1);
						canvas.drawBitmap(apollo, x - 20, y - 25, null);
						gameover = true;
					} else if (bottomLeft || bottomRight) {
						soundPool.play(explosion, 1, 3, 3, 0, 1);
						canvas.drawBitmap(explosion2, x - 25, y - 25, null);

						t = INITIAL_TIME; // reset the time variable

						gameover = true;
						downPressed = false;
						leftPressed = false;
						rightPressed = false;

					} else {
						canvas.drawBitmap(apollo, x - 20, y - 20, null);
						if (leftPressed == true) {
							canvas.drawBitmap(leftThruster, x - 20, y + 28, null);
						} else if (rightPressed == true) {
							canvas.drawBitmap(rightThruster, x + 22, y + 28, null);
						} else if (downPressed == true) {
							canvas.drawBitmap(mainThruster, x - 2, y + 28, null);
						}

					}
				}

				try {
					Thread.sleep(REFRESH_RATE);
				} catch (Exception e) {
				}

				finally {
					if (canvas != null) {
						holder.unlockCanvasAndPost(canvas);
					}
				}
			}
		}

	}

	public boolean contains(int[] xcor, int[] ycor, double x0, double y0) {
		int crossings = 0;

		for (int i = 0; i < xcor.length - 1; i++) {
			int x1 = xcor[i];
			int x2 = xcor[i + 1];

			int y1 = ycor[i];
			int y2 = ycor[i + 1];

			int dy = y2 - y1;
			int dx = x2 - x1;

			double slope = 0;
			if (dx != 0) {
				slope = (double) dy / dx;
			}

			boolean cond1 = (x1 <= x0) && (x0 < x2); 
			boolean cond2 = (x2 <= x0) && (x0 < x1); 
														
			boolean above = (y0 < slope * (x0 - x1) + y1); 

			if ((cond1 || cond2) && above) {
				crossings++;
			}
		}
		return (crossings % 2 != 0); 
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	public void surfaceCreated(SurfaceHolder holder) {
		main = new Thread(this);
		if (main != null)
			main.start();

	}

	

	public void reset() {
		gameover = false;

		x = width / 2;
		y = 0;
		t = INITIAL_TIME;

		downPressed = false;
		leftPressed = false;
		rightPressed = false;
		fuelLevel = 100;
		fuelBoost = 0;

	}
	
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		while (retry) {
			try {
				main.join();
				retry = false;
			} catch (InterruptedException e) {
				
			}
		}
	}



	public void rightThrust() {
		if (fuelLevel > 0) {
			soundPool.play(thruster, 1, 1, 1, 0, 1);
			x = x - 30;
			rightPressed = true;
			downPressed = false;
			leftPressed = false;
			fuelLevel = fuelLevel - 2;

		}
	}
	
	public void leftThrust() {
		if (fuelLevel > 0) {
			soundPool.play(thruster, 1, 1, 1, 0, 1);
			x = x + 30;
			leftPressed = true;
			rightPressed = false;
			downPressed = false;
			fuelLevel = fuelLevel - 5;

		}
	}
	
	public void fuellevel(Canvas canvas) {

		fuel.setColor(Color.GREEN);
		bar.setColor(Color.GRAY);
		canvas.drawRect(0, 0, 105, 25, bar);
		canvas.drawRect(2, 2, fuelLevel, 20, fuel);

	}
	
	public void landing(Canvas canvas) {
		landing = new Path();
		for (int j = 0; j < landX.length; j++) {
			landing.lineTo(landX[j], landY[j]);

		}
		landingSurface.setColor(Color.GREEN);
		canvas.drawPath(landing, landingSurface);
	}

	public void booster() {
		if (fuelLevel > 0) {
			soundPool.play(thruster, 1, 1, 1, 0, 1);
			fuelBoost = FUEL_BOOST;
			downPressed = true;
			leftPressed = false;
			rightPressed = false;
			fuelLevel = fuelLevel - 5;

		}
	}

	public void maintain() {
		fuelBoost = 0;
		downPressed = false;
		leftPressed = false;
		rightPressed = false;
		t = INITIAL_TIME;

	}

	



}
