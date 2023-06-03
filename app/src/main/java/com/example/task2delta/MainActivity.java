package com.example.task2delta;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private CircleView circleView;
    private SharedPreferences sharedPreferences;
    public int jumpedBlocks;
    private static int highest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        highest = sharedPreferences.getInt("highestScore", 0);


        // Create a LinearLayout to hold the custom view and button
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Create a custom view to draw circles
        circleView = new CircleView(this);
        layout.addView(circleView);

        // Set the LinearLayout as the main content view
        setContentView(layout);
    }

    private void startGame() {
        // Initialize the game state
        jumpedBlocks = 0;
        circleView.startGame();
        circleView.gameRunning = true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Retrieve the highest score from SharedPreferences
        highest = sharedPreferences.getInt("highestScore", 0);
        startGame();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Save the highest score to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("highestScore", highest);
        editor.apply();
    }

    public void setJumpedBlocks(int jumpedBlocks) {
        this.jumpedBlocks = jumpedBlocks;
    }

    // Custom View for drawing circles
    public static class CircleView extends View {

        private static final int CIRCLE1_RADIUS = 50;
        private static final int CIRCLE2_RADIUS = 100;
        private static final int BLOCK_WIDTH = 200;
        private static final int CIRCLE_JUMP_SPEED = 200;
        private static final int BLOCK_HEIGHT_RANGE=300;
        private static final int BLOCK_MOVE_SPEED = 20;  // Adjust the speed as needed

        private final Paint blockPaint;

        private final Handler handler;
        private final Runnable runnable;
        private int circle1Y;
        private int circle2Y;
        private int circle1X;
        private int circle2X;
        private int blockX;
        public int i=0;
        public int moveDistance=0;
        private int initialCircle1Y;
        private int blockHeight;
        private int initialCircle2Y;
        private int baseY;
        private int ceilingY;
        private final Paint circle1Paint;
        private final Paint circle2Paint;
        private int jumpedBlocks=0;
        public boolean gameRunning;
        private boolean firstCollision;

        public CircleView(Context context) {
            super(context);

            // Set up the paints for the circles
            circle1Paint = new Paint();
            circle1Paint.setColor(Color.BLUE);

            circle2Paint = new Paint();
            circle2Paint.setColor(Color.RED);
            blockPaint = new Paint();
            blockPaint.setColor(Color.GRAY);

            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    moveBlock();
                    invalidate();
                    checkCollision();
                    handler.postDelayed(this, 100); // Delay between block movement
                }
            };

            gameRunning = false;
            firstCollision = false;
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            // Initialize the initial positions of the circles
            circle1Y = h - CIRCLE1_RADIUS;
            circle2Y = h - CIRCLE2_RADIUS;
            circle1X=w/2;
            circle2X=w/3;
            initialCircle1Y = circle1Y;
            initialCircle2Y = circle2Y;
            blockHeight = generateRandomHeight(h);
            baseY = h;
            ceilingY = 0;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            // Draw the circles on the canvas
            canvas.drawCircle(circle1X, circle1Y, CIRCLE1_RADIUS, circle1Paint);
            canvas.drawCircle(circle2X, circle2Y, CIRCLE2_RADIUS, circle2Paint);

            // Draw the block on the canvas
            canvas.drawRect(blockX, canvas.getHeight() - blockHeight, blockX + 200,
                    canvas.getHeight(), blockPaint);

            Paint textPaint = new Paint();
            textPaint.setColor(Color.BLACK);
            textPaint.setTextSize(40);
            canvas.drawText("Highest Score: " + highest, 50, 50, textPaint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            // Detect touch events on the custom view
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                if (gameRunning) {
                    jumpSmallCircle();
                }
                return true;
            }
            return super.onTouchEvent(event);
        }

        public void startGame() {
            // Start the game and block movement
            jumpedBlocks = 0;
            circle1Y = initialCircle1Y;
            circle2Y = initialCircle2Y;
            blockX = getWidth();
            blockHeight = generateRandomHeight(getHeight());
            gameRunning = true;
            firstCollision = false;
            blockX -= BLOCK_MOVE_SPEED;
            handler.postDelayed(runnable, 100);
            }

        public void jumpSmallCircle() {
            // Move the small circle up by the jump speed
            circle1Y -= CIRCLE_JUMP_SPEED;
            circle1Y = Math.max(ceilingY + CIRCLE1_RADIUS, circle1Y);
            circle1Y = Math.min(baseY - CIRCLE1_RADIUS, circle1Y);
            // Invalidate the view to trigger a redraw
            invalidate();
        }
        private void moveBlock() {
            circle1Y += 30;
            // Move the block towards the left of the screen
            blockX -= BLOCK_MOVE_SPEED;
            blockX = getWidth() - 200;

            // Reset block position if it goes off the screen
            if (blockX + BLOCK_WIDTH < 0) {
                jumpedBlocks++;
                if (firstCollision) {
                    moveLargeCircle();
                }
                if (gameRunning) {
                    blockX = getWidth(); // Reset the block position to the right side of the screen
                    blockHeight = generateRandomHeight(getHeight()); // Generate a new random block height
                    handler.postDelayed(this::moveBlock, 100);
                }
            }


            // Invalidate the view to trigger a redraw
            invalidate();
        }

        private void moveLargeCircle() {
            circle2Y=2100-blockHeight;

            if(i==1&& moveDistance==0){
            if (circle2X > circle1X) {
                // Move the large circle closer to the small circle
                int distance = Math.abs(circle2X - circle1X);
                 moveDistance = Math.max(1, distance / 3);
                circle2X += moveDistance;
                circle2Y = initialCircle2Y;
            } }else {
                // Stop moving the large circle
                circle2Y = initialCircle2Y;
                handler.removeCallbacks(runnable);
            }

            // Invalidate the view to trigger a redraw
            invalidate();

        }

        private void checkCollision() {
            // Calculate the bounds of the circles and block
            int circle1Left = getWidth() / 3 - CircleView.CIRCLE1_RADIUS;
            int circle1Right = getWidth() / 3 + CIRCLE1_RADIUS;
            int circle1Top = circle1Y;
            int circle1Bottom = circle1Y + CIRCLE1_RADIUS;


            int blockLeft = blockX;
            int blockRight = blockX + BLOCK_WIDTH;
            int blockTop = getHeight() - blockHeight;
            int blockBottom = getHeight();
            blockX -= BLOCK_MOVE_SPEED;
            // Check for collision between the small circle and block
            if (circle1Right >= blockLeft && circle1Left <= blockRight &&
                    circle1Bottom >= blockTop && circle1Top <= blockBottom) {
                // Collision detected, move the large circle closer to the small circle
                firstCollision = true;
                i++;
                if(i>=2){
                    // Collision detected, end the game
                    handler.removeCallbacks(runnable); // Stop block movement

                    AlertDialog.Builder next = new AlertDialog.Builder(getContext());
                    next.setTitle("Game Over")
                            .setMessage("Highest score " + Math.max(highest, jumpedBlocks) + "\nNumber of blocks jumped: " + jumpedBlocks)
                            .setPositiveButton("Restart", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    startGame(); // Restart the game
                                }
                            })
                            .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    ((MainActivity) getContext()).finish();
                                }
                            })
                  .show();


                }


            }
            highest = Math.max(highest, jumpedBlocks);
        }

        private int generateRandomHeight(int blockHeight) {
            // Generate a random block height within the specified range
            Random random = new Random();
            return random.nextInt(BLOCK_HEIGHT_RANGE) + 200;
        }
    }
}

