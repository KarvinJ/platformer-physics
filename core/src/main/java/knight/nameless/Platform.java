package knight.nameless;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class Platform extends ApplicationAdapter {

    public final int SCREEN_WIDTH = 1280;
    public final int SCREEN_HEIGHT = 720;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    public OrthographicCamera camera = new OrthographicCamera();
    public ExtendViewport viewport;
    private final Array<Rectangle> structures = new Array<>();
    public final Vector2 velocity = new Vector2(0,0);
    private final Rectangle playerBounds = new Rectangle(400, 200, 32, 32);
    private final int speed = 50;

    @Override
    public void create() {

        camera.position.set(SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f, 0);
        viewport = new ExtendViewport(SCREEN_WIDTH, SCREEN_HEIGHT, camera);

        //the batch and shape needs to be initialized in the create method
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        structures.add(
            new Rectangle(100, 400, 200, 32),
            new Rectangle(400, 175, 200, 32),
            new Rectangle(225, 85, 200, 32),
            new Rectangle(700, 30, 150, 32)
        );

        structures.add(new Rectangle(0, 0, SCREEN_WIDTH, 32));
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    public Rectangle getPreviousPosition() {

        float positionX = playerBounds.x - velocity.x;
        float positionY = playerBounds.y - velocity.y;

        return new Rectangle(positionX, positionY, playerBounds.width, playerBounds.height);
    }

    private boolean checkCollisionInX(Rectangle player, Rectangle platform) {

        return player.x + player.width > platform.x
            && player.x < platform.x + platform.width;
    }

    private boolean checkCollisionInY(Rectangle player, Rectangle platform) {

        return player.y + player.height > platform.y
            && player.y < platform.y + platform.height;
    }

    private void managePlayerFloorCollision(float deltaTime) {

        for (Rectangle platform : structures) {

            if (playerBounds.overlaps(platform)) {

//                If the player previous position is within the x bounds of the platform,
//                then we need to resolve the collision by changing the y value
                if (checkCollisionInX(getPreviousPosition(), platform)) {

//                    Player was falling downwards. Resolve upwards.
                    if (velocity.y < 0)
                        playerBounds.y = platform.y + playerBounds.height;

//                     Player was moving upwards. Resolve downwards
                    else
                        playerBounds.y = platform.y - playerBounds.height;

                    velocity.y = 0;
                }
                //  If the player previous position is within the y bounds of the platform,
//                then we need to resolve the collision by changing the x value
                else if (checkCollisionInY(getPreviousPosition(), platform)) {

//                     Player was traveling right. Resolve to the left
                    if (velocity.x > 0)
                        playerBounds.x = platform.x - playerBounds.width;

//                     Player was traveling left. Resolve to the right
                    else
                        playerBounds.x = platform.x + platform.width;

                    velocity.x = 0;
                }

                if (velocity.y == 0 && Gdx.input.isKeyPressed(Input.Keys.SPACE))
                    velocity.y = 800 * deltaTime;
            }
        }
    }


    private void update(float deltaTime) {

        //gravity
        velocity.y -= 20.8f * deltaTime;

        //  Update the player's position
        playerBounds.y += velocity.y;
        playerBounds.x += velocity.x;

        // To avoid that my player keep going forward infinitely, I multiply the velocity, by my coefficient of friction 0.9
//        This will subtract 10% of the player's speed every frame, eventually bringing the player to a stop.
        velocity.x *= 0.9f;

//                -- Increase the player's x speed
        if (Gdx.input.isKeyPressed(Input.Keys.D))
            velocity.x += speed * deltaTime;

        else if (Gdx.input.isKeyPressed(Input.Keys.A))
            velocity.x -= speed * deltaTime;

        if(playerBounds.y < 0) {

            playerBounds.y = 600 - playerBounds.height;
            playerBounds.x = 200;
            velocity.y = 0;
        }

        managePlayerFloorCollision(deltaTime);
    }

    @Override
    public void render() {

        float deltaTime = Gdx.graphics.getDeltaTime();

        update(deltaTime);

        ScreenUtils.clear(Color.BLACK);

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(Color.DARK_GRAY);

        for (var structure :structures) {

            shapeRenderer.rect(structure.x, structure.y, structure.width, structure.height);
        }

        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(playerBounds.x, playerBounds.y, playerBounds.width, playerBounds.height);

        shapeRenderer.end();

//        batch.setProjectionMatrix(viewport.getCamera().combined);
//        batch.begin();
//
//        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
    }
}
