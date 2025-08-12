package knight.nameless;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.*;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import knight.nameless.objects.Enemy;
import knight.nameless.objects.GameObject;
import knight.nameless.objects.Player;

public class Platform extends ApplicationAdapter {

    public final int SCREEN_WIDTH = 960;
    public final int SCREEN_HEIGHT = 544;
    private ShapeRenderer shapeRenderer;
    public OrthographicCamera camera = new OrthographicCamera();
    public ExtendViewport viewport;
    private Player player;
    private TextureAtlas atlas;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer mapRenderer;
    private final Array<Rectangle> collisionBounds = new Array<>();
    private boolean isDebugRenderer = false;
    private boolean isDebugCamera = false;
    private final Array<GameObject> gameObjects = new Array<>();

    @Override
    public void create() {

        camera.position.set(SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f, 0);
        camera.zoom = 0.7f;
        viewport = new ExtendViewport(SCREEN_WIDTH, SCREEN_HEIGHT, camera);

        //the batch and shape needs to be initialized in the create method
        shapeRenderer = new ShapeRenderer();

        atlas = new TextureAtlas("images/sprites.atlas");
        player = new Player(new Rectangle(450, 50, 32, 32), atlas);

        gameObjects.add(player);

        tiledMap = new TmxMapLoader().load("maps/playground/test3.tmx");
        mapRenderer = setupMap(tiledMap);
    }

    public OrthogonalTiledMapRenderer setupMap(TiledMap tiledMap) {

        MapLayers mapLayers = tiledMap.getLayers();

        for (MapLayer mapLayer : mapLayers) {

            parseMapObjectsToBounds(mapLayer.getObjects(), mapLayer.getName());
        }

        return new OrthogonalTiledMapRenderer(tiledMap, 1);
    }

    private void parseMapObjectsToBounds(MapObjects mapObjects, String layerName) {

        for (MapObject mapObject : mapObjects) {

            Rectangle objectBounds = ((RectangleMapObject) mapObject).getRectangle();

            if (layerName.equals("Enemies"))
                gameObjects.add(new Enemy(objectBounds, atlas));
            else
                collisionBounds.add(objectBounds);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    private boolean checkCollisionInX(Rectangle bounds, Rectangle platform) {

        return bounds.x + bounds.width > platform.x
            && bounds.x < platform.x + platform.width;
    }

    private boolean checkCollisionInY(Rectangle bounds, Rectangle platform) {

        return bounds.y + bounds.height > platform.y
            && bounds.y < platform.y + platform.height;
    }

    private void manageStructureCollision(float deltaTime, GameObject gameObject) {

        for (var structure : collisionBounds) {

            if (gameObject.bounds.overlaps(structure)) {

//                If the player previous position is within the x bounds of the platform,
//                then we need to resolve the collision by changing the y value
                if (checkCollisionInX(gameObject.getPreviousPosition(), structure)) {

//                    Player was falling downwards. Resolve upwards.
                    if (gameObject.velocity.y < 0)
                        gameObject.bounds.y = structure.y + structure.height;

//                     Player was moving upwards. Resolve downwards
                    else
                        gameObject.bounds.y = structure.y - gameObject.bounds.height;

                    gameObject.velocity.y = 0;
                }
                //  If the player previous position is within the y bounds of the platform,
//                then we need to resolve the collision by changing the x value
                else if (checkCollisionInY(gameObject.getPreviousPosition(), structure)) {

//                     Player was traveling right. Resolve to the left
                    if (gameObject.velocity.x > 0)
                        gameObject.bounds.x = structure.x - gameObject.bounds.width;

//                     Player was traveling left. Resolve to the right
                    else
                        gameObject.bounds.x = structure.x + structure.width;

                    gameObject.velocity.x = 0;
                }
            }
        }

        //there is some inconsistencies with the jump sometimes.
        if (player.velocity.y == 0 && Gdx.input.isKeyPressed(Input.Keys.SPACE))
            player.velocity.y = 800 * deltaTime;
    }

    private void controlCameraPosition(OrthographicCamera camera) {

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            camera.position.x += 1;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
            camera.position.x -= 1;

        if (Gdx.input.isKeyPressed(Input.Keys.UP))
            camera.position.y += 1;

        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
            camera.position.y -= 1;

        if (Gdx.input.isKeyJustPressed(Input.Keys.F3))
            camera.zoom += 0.1f;

        if (Gdx.input.isKeyJustPressed(Input.Keys.F4))
            camera.zoom -= 0.1f;
    }

    public boolean isPlayerInsideMapBounds(Vector2 playerPixelPosition) {

        MapProperties properties = tiledMap.getProperties();

        int mapWidth = properties.get("width", Integer.class);
        int tilePixelWidth = properties.get("tilewidth", Integer.class);
        int mapPixelWidth = mapWidth * tilePixelWidth;

        var midScreenWidth = SCREEN_WIDTH / 2f;

        return playerPixelPosition.x > midScreenWidth && playerPixelPosition.x < mapPixelWidth - midScreenWidth;
    }

    private void update(float deltaTime) {

        for (GameObject gameObject : gameObjects) {

            gameObject.update(deltaTime);
            manageStructureCollision(deltaTime, gameObject);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F2))
            isDebugCamera = !isDebugCamera;

        if (isDebugCamera)
            controlCameraPosition(camera);

        var playerPosition = new Vector2(player.bounds.x, player.bounds.y);

        var isPlayerInsideMapBounds = isPlayerInsideMapBounds(playerPosition);

        if (!isDebugCamera && isPlayerInsideMapBounds)
            camera.position.set(playerPosition.x, 200, 0);

        camera.update();
    }

    void draw() {

        mapRenderer.setView(camera);

        mapRenderer.render();

        mapRenderer.getBatch().setProjectionMatrix(viewport.getCamera().combined);

        mapRenderer.getBatch().begin();

        for (GameObject gameObject : gameObjects) {

            gameObject.draw(mapRenderer.getBatch());
        }

        mapRenderer.getBatch().end();
    }

    @Override
    public void render() {

        float deltaTime = Gdx.graphics.getDeltaTime();

        System.out.println(deltaTime);

        update(deltaTime);

        if (Gdx.input.isKeyJustPressed(Input.Keys.F1))
            isDebugRenderer = !isDebugRenderer;

        ScreenUtils.clear(Color.BLACK);

        if (!isDebugRenderer)
            draw();
        else
            debugDraw();
    }

    private void debugDraw() {

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        shapeRenderer.setColor(Color.GREEN);

        for (var structure : collisionBounds) {

            shapeRenderer.rect(structure.x, structure.y, structure.width, structure.height);
        }

        shapeRenderer.setColor(Color.WHITE);

        for (var gameObject : gameObjects) {

            gameObject.draw(shapeRenderer);
        }

        shapeRenderer.end();
    }

    @Override
    public void dispose() {

        shapeRenderer.dispose();
        tiledMap.dispose();
        mapRenderer.dispose();
        atlas.dispose();

        for (var gameObject : gameObjects)
            gameObject.dispose();

        gameObjects.clear();
    }
}
