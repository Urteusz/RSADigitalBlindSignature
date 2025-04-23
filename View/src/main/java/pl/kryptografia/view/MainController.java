package pl.kryptografia.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import pl.kryptografia.model.*;


import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    BlindRSASignatureWithMenu rsa;
    byte[] bytesFromFile;
    byte[] bytesFromSignature;

    @FXML
    private TextField keyOne;

    @FXML
    private TextField keyTwo;

    @FXML
    private TextField keyThree;

    @FXML
    private TextField keyFour;

    @FXML
    private TextArea areaPlain;

    @FXML
    private TextArea areaEncrypted;

    @FXML
    private TextField textOpenPlain;

    @FXML
    private TextField textOpenEncrypted;

    @FXML
    private TextField textSavePlain;

    @FXML
    private TextField textSaveEncrypted;

    @FXML
    private RadioButton radioFile;

    @FXML
    private RadioButton radioText;

    @FXML
    private void onGenerateKeysClick() {
        rsa.generateKeys();
        keyOne.setText(BlindRSASignatureWithMenu.getN().toString());
        keyTwo.setText(BlindRSASignatureWithMenu.getE().toString());
        keyThree.setText(BlindRSASignatureWithMenu.getD().toString());
        keyFour.setText(BlindRSASignatureWithMenu.getR().toString());
    }

    @FXML
    private void openFileClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik do otwarcia");

        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            this.bytesFromFile = readFile(file);
            textOpenPlain.setText(file.getAbsolutePath());
            radioFile.setSelected(true);
            areaPlain.setDisable(true);
            areaEncrypted.setDisable(true);

            areaPlain.setPromptText("Plik zostal wczytany pomyslnie");
        }
    }

    @FXML
    private void openFileSinature() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik do otwarcia");

        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            this.bytesFromSignature = readFile(file);
            textOpenEncrypted.setText(file.getAbsolutePath());

            areaEncrypted.setText(bytesToHex(bytesFromSignature));
        }
    }

    @FXML
    private void onSignClick() {

        if (radioText.isSelected()) {
            String text = areaPlain.getText();
            bytesFromFile = text.getBytes();
            bytesFromSignature = rsa.signData(bytesFromFile);
        } else if (radioFile.isSelected()) {
            bytesFromSignature = rsa.signData(bytesFromFile);
        }

        if (bytesFromFile != null) {
            String hex = bytesToHex(bytesFromSignature);
            areaEncrypted.setText(hex);
            keyFour.setText(rsa.getR().toString());
        } else {
            areaEncrypted.setText("Błąd podpisywania.");
        }
    }

    @FXML
    private void onFileRadio() {
        areaPlain.setPromptText("Otworz plik do podpisania");
        areaPlain.setDisable(true);
    }

    @FXML
    private void onTextRadio() {
        areaPlain.setPromptText("Wpisz dane do podpisania");
        areaPlain.setDisable(false);
    }


    private void writeFile(byte[] content, int type) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz plik");

        // Add appropriate extension filters
        if (type == 1) {
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Text files", "*.txt"));
        } else {
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Signature files", "*.sig"));
        }

        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null) {
            try {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(content);
                fos.close();

                // Update the appropriate text field
                if (type == 1) {
                    textSavePlain.setText(file.getAbsolutePath());
                } else {
                    textSaveEncrypted.setText(file.getAbsolutePath());
                }
            } catch (IOException e) {
                showAlert("Błąd", "Błąd zapisu do pliku: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void savePlainClick() {
        if (radioText.isSelected()) {
            // Save text from area as bytes
            writeFile(areaPlain.getText().getBytes(), 1);
        } else if (radioFile.isSelected() && bytesFromFile != null) {
            // Save loaded file bytes
            writeFile(bytesFromFile, 1);
        } else {
            showAlert("Błąd", "Brak danych do zapisania", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void saveEncryptedClick() {
        if (radioText.isSelected()) {
            // Try to save hex string as bytes if possible
            String text = areaEncrypted.getText().trim();
            if (!text.isEmpty()) {
                try {
                    byte[] bytes = hexStringToByteArray(text);
                    writeFile(bytes, 2);
                } catch (Exception e) {
                    writeFile(text.getBytes(), 2);
                }
            } else {
                showAlert("Błąd", "Brak danych do zapisania", Alert.AlertType.ERROR);
            }
        } else if (radioFile.isSelected() && bytesFromSignature != null) {
            // Save loaded signature bytes
            writeFile(bytesFromSignature, 2);
        } else {
            showAlert("Błąd", "Brak danych do zapisania", Alert.AlertType.ERROR);
        }
    }


    @FXML
    private void onCheckSing() {
        String text = areaEncrypted.getText().trim();
        if (radioText.isSelected()) {
            String text_signed = areaPlain.getText().trim();
            if (text.isEmpty() || text_signed.isEmpty()) {
                showAlert("Błąd", "Brak danych do weryfikacji.", Alert.AlertType.ERROR);
                return;
            }
            bytesFromFile = text_signed.getBytes();
            bytesFromSignature = hexStringToByteArray(text);
        }

        if (bytesFromSignature != null && bytesFromFile != null) {
            boolean isValid = rsa.verifySignature(bytesFromSignature, bytesFromFile);
            if (isValid) {
                showAlert("Weryfikacja podpisu", "✅ Podpis jest poprawny.", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Weryfikacja podpisu", "❌ Podpis jest niepoprawny.", Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Błąd", "Nie udało się przeprowadzić weryfikacji.", Alert.AlertType.ERROR);
        }
    }



    private static byte[] readFile(File file) {
        try {
            byte[] bytes = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            fis.read(bytes);
            fis.close();
            return bytes;
        } catch (IOException e) {
            System.out.println("Błąd odczytu pliku: " + e.getMessage());
            return null;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        rsa = new BlindRSASignatureWithMenu();
        ToggleGroup fileOrTextGroup = new ToggleGroup();
        keyOne.setText(BlindRSASignatureWithMenu.getN().toString());
        keyTwo.setText(BlindRSASignatureWithMenu.getE().toString());
        keyThree.setText(BlindRSASignatureWithMenu.getD().toString());
        keyFour.setText("");
//        keyOne.setDisable(true);
//        keyTwo.setDisable(true);
//        keyThree.setDisable(true);
//        keyFour.setDisable(true);
        radioFile.setToggleGroup(fileOrTextGroup);
        radioText.setToggleGroup(fileOrTextGroup);
        areaPlain.setPromptText("Wpisz tekst do podpisania.");
        areaEncrypted.setPromptText("Wpisz lub otwórz podpis do walidacji.");
        radioText.setSelected(true);
    }
}