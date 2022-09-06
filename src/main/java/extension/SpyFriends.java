package extension;

import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionInfo;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import javafx.application.Platform;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

@ExtensionInfo(
        Title = "SpyFriends",
        Description = "Spy on your friends",
        Version = "1.1",
        Author = "Jalebi"
)
public class SpyFriends extends ExtensionForm {
    public CheckBox checkFollow;
    public CheckBox showGroup;

    public Label roomName;
    public Label roomDescription;
    public Label roomOwner;

    public Label roomNameText;
    public Label roomDescriptionText;
    public Label roomOwnerText;

    @Override
    protected void initExtension() {
        checkFollow.setSelected(true);
        showGroup.setSelected(true);

        // Runs this when the extension GUI is closed
        primaryStage.setOnHidden(e -> {
            checkFollow.setSelected(true);
            showGroup.setSelected(true);
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
            hMessage.getPacket().readInteger();
            String room = hMessage.getPacket().readString();
            Platform.runLater(() -> roomNameText.setText(room));
            hMessage.getPacket().readInteger();
            String owner = hMessage.getPacket().readString();
            Platform.runLater(() -> roomOwnerText.setText(owner));
            hMessage.getPacket().readInteger();
            hMessage.getPacket().readInteger();
            hMessage.getPacket().readInteger();
            String description = hMessage.getPacket().readString();
            Platform.runLater(() -> roomDescriptionText.setText(description));
        });
    }
}
