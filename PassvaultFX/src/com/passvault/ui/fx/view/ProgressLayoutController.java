package com.passvault.ui.fx.view;

import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ProgressLayoutController {

	@FXML
	Label titleLabel;
	@FXML
	ProgressIndicator indicator;
	
	private Stage stage;
	private Task<Object> task;
	private boolean running;
	
	private static Logger logger;
	
	static {
		logger = Logger.getLogger("com.passvault.ui.fx.view");
	}
	
	public ProgressLayoutController() {
		
	}
	
	
	@FXML
	private void initialize() {
		
	}
	
	
	public void setTitle(String title) {
		titleLabel.setText(title);
	}
	
	public void setStage(Stage stage) {
		this.stage = stage;
		
		stage.setOnShown(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
				logger.fine("Starting task");
				runTask();
			}
		});
	}
	
	
	public void setTask(Task<Object> task) {
		this.task = task;
	}
	
	
	public void runTask() {
		
		Platform.runLater(new Runnable() {
            @Override public void run() {
            		new Thread(task).start();
            		logger.fine("Task started");
            }
        });
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				
				while (!task.isDone()) {
					try {
						Thread.sleep(200L);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					logger.finest("Task still running");
				}
				
				logger.finest("Task Complete.");
				
				/*
				Platform.runLater(new Runnable() {
		            @Override public void run() {
		            		titleLabel.setText("Complete!!");
						indicator.setProgress(1.0);
		            }
		        });
				
				try {
					Thread.sleep(1_000L);
				} catch (InterruptedException e) { e.printStackTrace(); }
				*/
				
				Platform.runLater(new Runnable() {
		            @Override public void run() {
		                stage.close();
		            }
		        });
		        
			}
		}).start();
	}
	
}
