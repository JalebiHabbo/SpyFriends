package extension;

import gearth.extensions.ThemedExtensionFormCreator;
import java.net.URL;

public class SpyFriendsLauncher extends ThemedExtensionFormCreator {

    @Override
    protected String getTitle() {
        return "Spy Friends";
    }

    @Override
    protected URL getFormResource() {
        return getClass().getResource("SpyFriends.fxml");
    }

    public static void main(String[] args) {
        runExtensionForm(args, SpyFriendsLauncher.class);
    }
}
