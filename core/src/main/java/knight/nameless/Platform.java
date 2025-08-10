package knight.nameless;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
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
    private Player player;
    private OrthogonalTiledMapRenderer mapRenderer;
    private final Array<Rectangle> collisionRectangles = new Array<>();

    @Override
    public void create() {

        camera.position.set(SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f, 0);
        viewport = new ExtendViewport(SCREEN_WIDTH, SCREEN_HEIGHT, camera);

        //the batch and shape needs to be initialized in the create method
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        player = new Player(new Rectangle(400, 150, 32, 32));

        structures.add(
            new Rectangle(100, 400, 200, 32),
            new Rectangle(400, 175, 200, 32),
            new Rectangle(225, 85, 200, 32),
            new Rectangle(700, 30, 150, 32)
        );

        structures.add(new Rectangle(0, 0, SCREEN_WIDTH, 32));

        TiledMap tiledMap = new TmxMapLoader().load("maps/playground/test3.tmx");

        mapRenderer = setupMap(tiledMap);
    }

    public OrthogonalTiledMapRenderer setupMap(TiledMap tiledMap) {

        MapLayers mapLayers = tiledMap.getLayers();

        for (MapLayer mapLayer : mapLayers)
            parseMapObjectsToBounds(mapLayer.getObjects());

        return new OrthogonalTiledMapRenderer(tiledMap, 1);
    }

    private void parseMapObjectsToBounds(MapObjects mapObjects) {

        for (MapObject mapObject : mapObjects) {

            Rectangle rectangle = ((RectangleMapObject) mapObject).getRectangle();

            collisionRectangles.add(rectangle);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
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

        //it seems that the collision only works if the height of the rectangle is less or equal than 32
        for (Rectangle platform : collisionRectangles) {

            if (player.bounds.overlaps(platform)) {

//                If the player previous position is within the x bounds of the platform,
//                then we need to resolve the collision by changing the y value
                if (checkCollisionInX(player.getPreviousPosition(), platform)) {

//                    Player was falling downwards. Resolve upwards.
                    if (player.velocity.y < 0)
                        player.bounds.y = platform.y + player.bounds.height;

//                     Player was moving upwards. Resolve downwards
                    else
                        player.bounds.y = platform.y - player.bounds.height;

                    player.velocity.y = 0;
                }
                //  If the player previous position is within the y bounds of the platform,
//                then we need to resolve the collision by changing the x value
                else if (checkCollisionInY(player.getPreviousPosition(), platform)) {

//                     Player was traveling right. Resolve to the left
                    if (player.velocity.x > 0)
                        player.bounds.x = platform.x - player.bounds.width;

//                     Player was traveling left. Resolve to the right
                    else
                        player.bounds.x = platform.x + platform.width;

                    player.velocity.x = 0;
                }

                if (player.velocity.y == 0 && Gdx.input.isKeyPressed(Input.Keys.SPACE))
                    player.velocity.y = 800 * deltaTime;
            }
        }
    }

    private void update(float deltaTime) {

        player.update(deltaTime);

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

//        for (var structure :structures) {
//
//            shapeRenderer.rect(structure.x, structure.y, structure.width, structure.height);
//        }

        for (var structure :collisionRectangles) {

            shapeRenderer.rect(structure.x, structure.y, structure.width, structure.height);
        }

        shapeRenderer.setColor(Color.WHITE);
        player.draw(shapeRenderer);

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
        mapRenderer.dispose();
    }
}
