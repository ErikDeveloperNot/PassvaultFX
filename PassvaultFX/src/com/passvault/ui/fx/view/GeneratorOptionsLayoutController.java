package com.passvault.ui.fx.view;

import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.passvault.ui.fx.Passvault;
import com.passvault.util.data.file.model.Settings;
import com.passvault.ui.fx.utils.Utils;
import com.passvault.util.DefaultRandomPasswordGenerator;
import com.passvault.util.RandomPasswordGenerator;

import javafx.fxml.FXML;
//import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class GeneratorOptionsLayoutController {

	@FXML
	TextArea allowedCharactersTextArea;
	@FXML
	CheckBox upperCheckBox;
	@FXML
	CheckBox lowerCheckBox;
	@FXML
	CheckBox digitsCheckBox;
	@FXML
	CheckBox specialCheckBox;
	@FXML
	TextField lengthTextField;
	@FXML
	Button saveButton;
	@FXML
	Button cancelButton;

	private Stage stage;
	private DefaultRandomPasswordGenerator generator;
	private boolean useCustomGenerator;
	private TreeSet<Character> currentChars;
	private int length;
	
	private static Logger logger;
	
	static {
		logger = Logger.getLogger("com.passvault.ui.fx.view");
	}
	
	public GeneratorOptionsLayoutController() {
		//generator = new DefaultRandomPasswordGenerator();
		Settings settings = Passvault.getSettings();
		
		// if Settings exist and the default generator is overridden use it as the new default
		if (settings != null) {
			generator = settings.getDefaultRandomPasswordGenerator();
		}
		
		if (generator == null)
			generator = new DefaultRandomPasswordGenerator();

		logger.fine(generator.getLength() + "\n" + generator.getAllowedCharactres());
		useCustomGenerator = false;
	}
	
	
	@FXML
	private void initialize() {
		saveButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> savePressed()));
		saveButton.setTooltip(new Tooltip("Save generator options"));
		cancelButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> cancelPressed()));
		cancelButton.setTooltip(new Tooltip("Cancel without saving generator options"));
		setGenerator(generator);
		allowedCharactersTextArea.setOnKeyPressed((KeyEvent e) -> {
			if (e.getCode().equals(KeyCode.TAB)) {
				lengthTextField.requestFocus();
				e.consume();
			}
		});
		
	}
	
	@FXML
	private void savePressed() {
		logger.fine("Saved Pressed");

		if (!verifyState()) {
			logger.warning("Verify state failed");
			return;
		}
		
		setGeneratorOptions();
		useCustomGenerator = true;
		logger.fine("Returning from SAVE");
		stage.close();
	}
	
	@FXML
	private void cancelPressed() {
		useCustomGenerator = false;
		stage.close();
	}
	
	@FXML
	private void upperCheckAccessed() {
		if (!updateSetWithTextArea())
			return;
		
		boolean add = upperCheckBox.isSelected();
		logger.finest("Upper checked: " + add);
		
		if (updateCurrentChars(generator.getDefaultUpper(), add))
			setAllowedTextArea();
	}
	
	@FXML
	private void lowerCheckAccessed() {
		if (!updateSetWithTextArea())
			return;
		
		boolean add = lowerCheckBox.isSelected();
		logger.finest("Lower checked: " + add);
		
		if (updateCurrentChars(generator.getDefaultLower(), add))
			setAllowedTextArea();
	}
	
	@FXML
	private void digitsCheckAccessed() {
		if (!updateSetWithTextArea())
			return;
		
		boolean add = digitsCheckBox.isSelected();
		logger.finest("Digits checked: " + add);
		
		if (updateCurrentChars(generator.getDefaultDigits(), add))
			setAllowedTextArea();
	}
	
	@FXML
	private void specialCheckAccessed() {
		if (!updateSetWithTextArea())
			return;
		
		boolean add = specialCheckBox.isSelected();
		logger.finest("Special checked: " + add);
		
		if (updateCurrentChars(generator.getDefaultSpecial(), add))
			setAllowedTextArea();
	}
	
	
	private boolean updateCurrentChars(char[] chars, boolean add) {
		boolean setUpdated = false;
		
		if (add) {
			for (char c : chars) {
				if (currentChars.add(c))
					setUpdated = true;
			}
		} else {
			for (char c : chars) {
				if (currentChars.remove(c))
					setUpdated = true;
			}
		}
		
		return setUpdated;
	}
	
	
	private boolean updateSetWithTextArea() {
		String text = allowedCharactersTextArea.getText().trim();
		logger.fine("Allowed chanracters text area before processing:\n" + text);
		String[] tokens = text.split("\\s+");
		TreeSet<Character> set = new TreeSet<>();
		
		for (String token : tokens) {
			if(token.length() == 0)
				continue;
			
			if (token.length() != 1) {
				logger.warning("Illegal options character, " + token.length() + ", '" + token + "'");
				/*Alert alert = new Alert(AlertType.ERROR);
	            alert.initOwner(stage);
	            alert.setTitle("Character Error");
	            alert.setHeaderText("Invalid Character");
	            alert.setContentText("Verify that there is a space between every character");
	            alert.showAndWait();*/
	            
	            Utils.showAlert(AlertType.ERROR, stage, "Character Error", "Invalid Character",
	            		"Verify that there is a space between every character");
	            
				return false;
			}
			
			set.add(token.charAt(0));
		}
		
		currentChars = set;
		
		if (currentChars.size() < 1) {
			logger.warning("Generator needs to allow at least 1 character");
			Utils.showAlert(AlertType.ERROR, stage, "Character Error", "No Characters",
            		"At least 1 character needs to be allowed for the gnereator");
			return false;
		}
			
		logger.fine("updateSetWithTextArea returning: TRUE");
		return true;
	}
	
	
	private void setAllowedTextArea() {
		StringBuilder builder = new StringBuilder();
		
		for (Character character : currentChars) {
			builder.append(character + " ");
		}
		
		allowedCharactersTextArea.setText(builder.toString());
	}
	
	
	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	public boolean useCustomGenerator() {
		return useCustomGenerator;
	}
	
	public RandomPasswordGenerator getGenerator() {
		return generator;
	}
	
	public boolean verifyState() {
		
		if (!updateSetWithTextArea()) {
			logger.finest("verifyState returning: FALSE");
			return false;
		}
		
		boolean invalidNumber = false;
		length = generator.getLength();
		
		try {
			length = Integer.parseInt(lengthTextField.getText());
		} catch(NumberFormatException e) {
			invalidNumber = true;
		}

		if (invalidNumber || length < 1 || length > 64) {
			logger.warning("Invalid number or length entered");
			/*Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(stage);
            alert.setTitle("Length Error");
            alert.setHeaderText("Invalid Length");
            alert.setContentText("The length must be between 1 and 64");
            alert.showAndWait();*/
            
            Utils.showAlert(AlertType.ERROR, stage, "Length Error", "Invalid Length",
            		"The length must be between 1 and 64");
            
            return false;
		}
		
		return true;
	}
	
	
	public void setGeneratorOptions() {
		generator.setLength(length);
		
		// Constraints could be off if sub-group is not checked but then added manually, oh well
		generator.setCheckUpper(upperCheckBox.isSelected());
		generator.setCheckLower(lowerCheckBox.isSelected());
		generator.setCheckDigits(digitsCheckBox.isSelected());
		generator.setCheckSpecial(specialCheckBox.isSelected());
		
		generator.clearAllowedCharacters();
		generator.setAllowedCharacters(currentChars);
		useCustomGenerator = true;
		logger.fine("Custom generator set with length: " + length + " with characters:\n" + generator.getAllowedCharactres());
	}
	
	
	public void setGenerator(DefaultRandomPasswordGenerator rpg) {
		List<Character> chars = rpg.getAllowedCharactres();
		upperCheckBox.setSelected(rpg.isCheckUpper());
		lowerCheckBox.setSelected(rpg.isCheckLower());
		digitsCheckBox.setSelected(rpg.isCheckDigits());
		specialCheckBox.setSelected(rpg.isCheckSpecial());
		lengthTextField.setText(Integer.toString(rpg.getLength()));
		currentChars = new TreeSet<>(chars);
		setAllowedTextArea();
		
		logger.finest("Generator set with length: " + rpg.getLength() + ", using characters:\n" + rpg.getAllowedCharactres());
	}
}
