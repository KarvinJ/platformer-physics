package knight.nameless;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
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

public class Platform extends ApplicationAdapter {

    public final int SCREEN_WIDTH = 960;
    public final int SCREEN_HEIGHT = 544;
    private ShapeRenderer shapeRenderer;
    public OrthographicCamera camera = new OrthographicCamera();
    public ExtendViewport viewport;
    private Player player;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer mapRenderer;
    private final Array<Rectangle> collisionBounds = new Array<>();
    private boolean isDebugRenderer = false;
    private boolean isDebugCamera = false;

    @Override
    public void create() {

        camera.position.set(SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f, 0);
        viewport = new ExtendViewport(SCREEN_WIDTH, SCREEN_HEIGHT, camera);

        //the batch and shape needs to be initialized in the create method
        shapeRenderer = new ShapeRenderer();

        player = new Player(new Rectangle(500, 300, 32, 32));

        tiledMap = new TmxMapLoader().load("maps/playground/test3.tmx");
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

            collisionBounds.add(rectangle);
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

    private void managePlayerAndStructureCollision(float deltaTime) {

        for (var structure : collisionBounds) {

            if (player.bounds.overlaps(structure)) {

//                If the player previous position is within the x bounds of the platform,
//                then we need to resolve the collision by changing the y value
                if (checkCollisionInX(player.getPreviousPosition(), structure)) {

//                    Player was falling downwards. Resolve upwards.
                    if (player.velocity.y < 0)
                        player.bounds.y = structure.y + structure.height;

//                     Player was moving upwards. Resolve downwards
                    else
                        player.bounds.y = structure.y - player.bounds.height;

                    player.velocity.y = 0;
                }
                //  If the player previous position is within the y bounds of the platform,
//                then we need to resolve the collision by changing the x value
                else if (checkCollisionInY(player.getPreviousPosition(), structure)) {

//                     Player was traveling right. Resolve to the left
                    if (player.velocity.x > 0)
                        player.bounds.x = structure.x - player.bounds.width;

//                     Player was traveling left. Resolve to the right
                    else
                        player.bounds.x = structure.x + structure.width;

                    player.velocity.x = 0;
                }

                //there is some inconsistencies with the jump sometimes.
                if (player.velocity.y == 0 && Gdx.input.isKeyPressed(Input.Keys.SPACE))
                    player.velocity.y = 800 * deltaTime;
            }
        }
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

    public boolean isPlayerInsideMapBounds(Vector2 playerPixelPosition){

        MapProperties properties = tiledMap.getProperties();

        int mapWidth = properties.get("width", Integer.class);
//        int mapHeight = properties.get("height", Integer.class);
        int tilePixelWidth = properties.get("tilewidth", Integer.class);
//        int tilePixelHeight = properties.get("tileheight", Integer.class);

        int mapPixelWidth = mapWidth * tilePixelWidth;
//        int mapPixelHeight = mapHeight * tilePixelHeight;

        var midScreenWidth = SCREEN_WIDTH / 2f;

        return playerPixelPosition.x > midScreenWidth && playerPixelPosition.x < mapPixelWidth - midScreenWidth;
    }

    private void update(float deltaTime) {

        player.update(deltaTime);

        managePlayerAndStructureCollision(deltaTime);

        if (Gdx.input.isKeyJustPressed(Input.Keys.F2))
            isDebugCamera = !isDebugCamera;

        if (isDebugCamera)
            controlCameraPosition(camera);

        var playerPosition = new Vector2(player.bounds.x, player.bounds.y);

        var isPlayerInsideMapBounds = isPlayerInsideMapBounds(playerPosition);

        if (!isDebugCamera && isPlayerInsideMapBounds)
            camera.position.set(player.bounds.x, 275, 0);

        camera.update();
    }

    void draw() {

        mapRenderer.setView(camera);

        mapRenderer.render();

        mapRenderer.getBatch().setProjectionMatrix(viewport.getCamera().combined);

        mapRenderer.getBatch().begin();

//        player.draw(mapRenderer.getBatch());

        mapRenderer.getBatch().end();
    }

    @Override
    public void render() {

        float deltaTime = Gdx.graphics.getDeltaTime();

        update(deltaTime);

        if (Gdx.input.isKeyJustPressed(Input.Keys.F1))
            isDebugRenderer = !isDebugRenderer;

        ScreenUtils.clear(Color.BLACK);

        if (!isDebugRenderer)
            draw();

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(Color.GREEN);

        if (isDebugRenderer) {

            for (var structure : collisionBounds) {

                shapeRenderer.rect(structure.x, structure.y, structure.width, structure.height);
            }
        }

        shapeRenderer.setColor(Color.WHITE);
        player.draw(shapeRenderer);

        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        tiledMap.dispose();
        mapRenderer.dispose();
    }
}
