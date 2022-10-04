package extension;

import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionInfo;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import gearth.protocol.connection.HClient;
import javafx.application.Platform;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

@ExtensionInfo(
        Title = "SpyFriends",
        Description = "Spy on your friends",
        Version = "1.2",
        Author = "Jalebi"
)
public class SpyFriends extends ExtensionForm {
    public CheckBox checkFollow;
    public CheckBox showGroup;
    public CheckBox showPicture;

    public Label roomName;
    public Label roomDescription;
    public Label roomOwner;
    public Label roomCount;

    public Label roomNameText;
    public Label roomDescriptionText;
    public Label roomOwnerText;
    public Label roomCountText;

    public ImageView roomPictureView;
    public Image roomPicture;

    public String hotelLanguage;

    private void doOnConnect(String host, int connectionPort, String hotelVersion, String clientIdentifier, HClient clientType) {
        hotelLanguage = host.substring(5, 7);
    }

    private void setImage(int roomCode) {
        String baseUrl = "https://habbo-stories-content.s3.amazonaws.com/navigator-thumbnail/hh";
        roomPicture = new Image(baseUrl + hotelLanguage + "/" + roomCode + ".png");
        if (roomPicture.errorProperty().getValue()) {
            roomPicture = new Image(getClass().getResource("default_room.png").toExternalForm());
        }
        roomPictureView.setImage(roomPicture);
    }

    @Override
    protected void initExtension() {
        this.onConnect(this::doOnConnect);
        checkFollow.setSelected(true);
        showGroup.setSelected(true);
        showPicture.setSelected(true);

        // Runs this when the extension GUI is closed
        primaryStage.setOnHidden(e -> {
            checkFollow.setSelected(true);
            showGroup.setSelected(true);
            showPicture.setSelected(true);
        });

        // Intercept the room forward message and block it so you dont get redirected
        intercept(HMessage.Direction.TOCLIENT, "RoomForward", hMessage -> {
            if (!checkFollow.isSelected()) {
                hMessage.setBlocked(true);
                int roomCode = hMessage.getPacket().readInteger();
                sendToServer(new HPacket("GetGuestRoom", HMessage.Direction.TOSERVER, roomCode, 1, 0));
            }
        });

        // For if you want to disable the group showing when following friends
        intercept(HMessage.Direction.TOCLIENT, "HabboGroupDetails", hMessage -> {
            if (!showGroup.isSelected()) {
                hMessage.setBlocked(true);
            }
        });

        // intercept the results of the room and print them on screen
        intercept(HMessage.Direction.TOCLIENT, "GetGuestRoomResult", hMessage -> {
            hMessage.getPacket().readBoolean();
            int roomCode = hMessage.getPacket().readInteger();
            if (showPicture.isSelected()) {
                setImage(roomCode);
            } else {
                roomPictureView.setImage(null);
            }
            String room = hMessage.getPacket().readString();
            Platform.runLater(() -> roomNameText.setText(room));
            hMessage.getPacket().readInteger();
            String owner = hMessage.getPacket().readString();
            Platform.runLater(() -> roomOwnerText.setText(owner));
            hMessage.getPacket().readInteger();
            int users = hMessage.getPacket().readInteger();
            Platform.runLater(() -> roomCountText.setText(String.valueOf(users)));
            hMessage.getPacket().readInteger();
            String description = hMessage.getPacket().readString();
            Platform.runLater(() -> roomDescriptionText.setText(description));
        });
    }
}
