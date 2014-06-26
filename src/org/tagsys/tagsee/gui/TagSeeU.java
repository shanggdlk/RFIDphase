package org.tagsys.tagsee.gui;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class TagSeeU extends Application  {

	private final double MINIMUM_WINDOW_WIDTH = 1100.0;
	private final double MINIMUM_WINDOW_HEIGHT = 700.0;

	
	public void collectData(String ip, int time) throws Exception {

		
	}

	@Override
	public void start(Stage stage) {
		// Only show root events from the base logger
		stage.setTitle("TagSee 1.0");
		stage.setMinWidth(MINIMUM_WINDOW_WIDTH);
		stage.setMinHeight(MINIMUM_WINDOW_HEIGHT);
		stage.setWidth(MINIMUM_WINDOW_WIDTH);
		stage.setHeight(MINIMUM_WINDOW_HEIGHT);
		
		
		try {
			Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));		
			stage.setScene(new Scene(root, MINIMUM_WINDOW_WIDTH, MINIMUM_WINDOW_HEIGHT));
			stage.sizeToScene();
	        stage.show();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	    
        stage.show();
	}

//	private Initializable replaceSceneContent(InputStream in, URL url)
//			throws Exception {
//		FXMLLoader loader = new FXMLLoader();
//		loader.setBuilderFactory(new JavaFXBuilderFactory());
//		loader.setLocation(url);
//		AnchorPane page;
//		try {
//			page = (AnchorPane) loader.load(in);
//		} finally {
//			in.close();
//		}
//		Scene scene = new Scene(page, this.MINIMUM_WINDOW_WIDTH,
//				this.MINIMUM_WINDOW_HEIGHT);
//		stage.setScene(scene);
//		stage.sizeToScene();
//		return (Initializable) loader.getController();
//	}

	public static void main(String[] args) {

		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.ERROR);

		launch(args);
	}

}
