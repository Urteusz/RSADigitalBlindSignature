package pl.kryptografia.view;

import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import pl.kryptografia.model.BlindRSASignature;

public class MainController implements Initializable {

    // Główna instancja klasy obsługującej podpisywanie RSA z oślepieniem
    private BlindRSASignature rsa;

    // Bufory na dane z plików
    private byte[] bytesFromFile;
    private byte[] bytesFromSignature;

    // Pola tekstowe dla kluczy
    @FXML private TextField keyOne;    // Moduł N
    @FXML private TextField keyTwo;    // Klucz publiczny E
    @FXML private TextField keyThree;  // Klucz prywatny D
    @FXML private TextField keyFour;   // Losowa liczba K

    // Obszary tekstowe na dane i podpis
    @FXML private TextArea areaPlain;
    @FXML private TextArea areaEncrypted;

    // Pola tekstowe na ścieżki plików
    @FXML private TextField textOpenPlain;
    @FXML private TextField textOpenEncrypted;
    @FXML private TextField textSavePlain;
    @FXML private TextField textSaveEncrypted;

    // Radio przyciski do wyboru trybu wejścia/wyjścia
    @FXML private RadioButton radioFile;
    @FXML private RadioButton radioText;

    // Generowanie nowych kluczy i aktualizacja pól
    @FXML
    private void onGenerateKeysClick() {
        rsa.generateKeys();
        keyOne.setText(BlindRSASignature.getN().toString());
        keyTwo.setText(BlindRSASignature.getE().toString());
        keyThree.setText(BlindRSASignature.getD().toString());
        keyFour.setText(BlindRSASignature.getK().toString());
    }

    // Wczytanie danych z pliku do podpisania
    @FXML
    private void openFileClick() {
        File file = chooseFile("Wybierz plik do otwarcia");
        if (file != null) {
            bytesFromFile = readFile(file);
            textOpenPlain.setText(file.getAbsolutePath());
            radioFile.setSelected(true);
            areaPlain.setDisable(true);
            areaEncrypted.setDisable(true);
            areaPlain.setPromptText("Plik zostal wczytany pomyslnie");
        }
    }

    // Wczytanie podpisu z pliku
    @FXML
    private void openFileSinature() {
        File file = chooseFile("Wybierz plik do otwarcia");
        if (file != null) {
            bytesFromSignature = readFile(file);
            textOpenEncrypted.setText(file.getAbsolutePath());
            areaEncrypted.setText(bytesToHex(bytesFromSignature));
        }
    }

    // Wczytanie kluczy z plików .priv i .pub
    @FXML
    private void onLoadKeys() {
        File priv = chooseFile("Wczytaj klucz prywatny", "Klucz prywatny (*.priv)", "*.priv");
        if (priv == null) {
            showAlert("Błąd", "Nie wybrano klucza prywatnego.", Alert.AlertType.ERROR);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(priv))) {
            BigInteger d = new BigInteger(reader.readLine().trim());
            BigInteger n = new BigInteger(reader.readLine().trim());
            BlindRSASignature.setD(d);
            BlindRSASignature.setN(n);
            keyThree.setText(d.toString());
            keyOne.setText(n.toString());
        } catch (Exception e) {
            showAlert("Błąd", "Błąd wczytywania klucza prywatnego: " + e.getMessage(), Alert.AlertType.ERROR);
            return;
        }

        File pub = chooseFile("Wczytaj klucz publiczny", "Klucz publiczny (*.pub)", "*.pub");
        if (pub == null) {
            showAlert("Błąd", "Nie wybrano klucza publicznego.", Alert.AlertType.ERROR);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(pub))) {
            BigInteger e = new BigInteger(reader.readLine().trim());
            BigInteger n = new BigInteger(reader.readLine().trim());
            if (!BlindRSASignature.getN().equals(n)) {
                showAlert("Błąd", "Klucz publiczny i prywatny mają różne N.", Alert.AlertType.ERROR);
                return;
            }
            BlindRSASignature.setE(e);
            keyTwo.setText(e.toString());
        } catch (Exception e) {
            showAlert("Błąd", "Błąd wczytywania klucza publicznego: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Zapisanie danych (oryginalnych lub podpisanych) do pliku
    @FXML
    private void savePlainClick() {
        if (radioText.isSelected()) {
            writeFile(areaPlain.getText().getBytes(), 1);
        } else if (radioFile.isSelected() && bytesFromFile != null) {
            writeFile(bytesFromFile, 1);
        } else {
            showAlert("Błąd", "Brak danych do zapisania", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void saveEncryptedClick() {
        if (radioText.isSelected()) {
            String text = areaEncrypted.getText().trim();
            if (!text.isEmpty()) {
                try {
                    writeFile(hexStringToByteArray(text), 2);
                } catch (Exception e) {
                    writeFile(text.getBytes(), 2);
                }
            } else {
                showAlert("Błąd", "Brak danych do zapisania", Alert.AlertType.ERROR);
            }
        } else if (radioFile.isSelected() && bytesFromSignature != null) {
            writeFile(bytesFromSignature, 2);
        } else {
            showAlert("Błąd", "Brak danych do zapisania", Alert.AlertType.ERROR);
        }
    }

    // Zapisanie kluczy do plików .priv i .pub
    @FXML
    private void onSaveKeys() {
        saveKeyToFile("Zapisz klucz prywatny", "Private Key (*.priv)", "*.priv",
                BlindRSASignature.getD(), BlindRSASignature.getN());
        saveKeyToFile("Zapisz klucz publiczny", "Public Key (*.pub)", "*.pub",
                BlindRSASignature.getE(), BlindRSASignature.getN());
    }

    // Podpisywanie danych
    @FXML
    private void onSignClick() {
        if (radioText.isSelected()) {
            bytesFromFile = areaPlain.getText().getBytes();
        }
        if (bytesFromFile != null) {
            bytesFromSignature = rsa.signData(bytesFromFile);
            areaEncrypted.setText(bytesToHex(bytesFromSignature));
            keyFour.setText(rsa.getK().toString());
        } else {
            areaEncrypted.setText("Błąd podpisywania.");
        }
    }

    // Weryfikacja podpisu
    @FXML
    private void onCheckSing() {
        if (radioText.isSelected()) {
            if (areaPlain.getText().isEmpty() || areaEncrypted.getText().isEmpty()) {
                showAlert("Błąd", "Brak danych do weryfikacji.", Alert.AlertType.ERROR);
                return;
            }
            bytesFromFile = areaPlain.getText().getBytes();
            bytesFromSignature = hexStringToByteArray(areaEncrypted.getText());
        }

        if (bytesFromFile != null && bytesFromSignature != null) {
            boolean result = rsa.verifySignature(bytesFromSignature, bytesFromFile);
            showAlert("Weryfikacja podpisu",
                    result ? "✅ Podpis jest poprawny." : "❌ Podpis jest niepoprawny.",
                    result ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        } else {
            showAlert("Błąd", "Nie udało się przeprowadzić weryfikacji.", Alert.AlertType.ERROR);
        }
    }

    // Obsługa zmiany trybu wejścia
    @FXML
    private void onFileRadio() {
        areaPlain.setDisable(true);
        areaPlain.setPromptText("Otworz plik do podpisania");
    }

    @FXML
    private void onTextRadio() {
        areaPlain.setDisable(false);
        areaPlain.setPromptText("Wpisz dane do podpisania");
    }

    // Metoda pomocnicza do wyboru pliku
    private File chooseFile(String title) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        return fc.showOpenDialog(new Stage());
    }

    private File chooseFile(String title, String desc, String ext) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(desc, ext));
        return fc.showOpenDialog(new Stage());
    }

    // Metoda do zapisu plików
    private void writeFile(byte[] content, int type) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Zapisz plik");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(type == 1 ? "Text files" : "Signature files",
                        type == 1 ? "*.txt" : "*.sig"));

        File file = fc.showSaveDialog(new Stage());
        if (file != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(content);
                if (type == 1) textSavePlain.setText(file.getAbsolutePath());
                else textSaveEncrypted.setText(file.getAbsolutePath());
            } catch (IOException e) {
                showAlert("Błąd", "Błąd zapisu do pliku: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    // Zapis kluczy do plików
    private void saveKeyToFile(String title, String desc, String ext, BigInteger val1, BigInteger val2) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(desc, ext));
        File file = fc.showSaveDialog(new Stage());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println(val1.toString());
                writer.println(val2.toString());
            } catch (IOException e) {
                showAlert("Błąd", "Nie udało się zapisać klucza: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    // Pomocnicze konwersje
    private byte[] readFile(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] bytes = new byte[(int) file.length()];
            fis.read(bytes);
            return bytes;
        } catch (IOException e) {
            System.out.println("Błąd odczytu pliku: " + e.getMessage());
            return null;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return sb.toString();
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // Inicjalizacja GUI i domyślnych wartości
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        rsa = new BlindRSASignature();

        ToggleGroup group = new ToggleGroup();
        radioFile.setToggleGroup(group);
        radioText.setToggleGroup(group);
        radioText.setSelected(true);

        areaPlain.setPromptText("Wpisz tekst do podpisania.");
        areaEncrypted.setPromptText("Wpisz lub otwórz podpis do walidacji.");

        keyOne.setText(BlindRSASignature.getN().toString());
        keyTwo.setText(BlindRSASignature.getE().toString());
        keyThree.setText(BlindRSASignature.getD().toString());
        keyFour.setText("");

        keyOne.setDisable(true);
        keyTwo.setDisable(true);
        keyThree.setDisable(true);
        keyFour.setDisable(true);
    }
}