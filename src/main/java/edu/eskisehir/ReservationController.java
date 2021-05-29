package edu.eskisehir;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.IndexedCheckModel;

import java.io.File;
import java.net.URL;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReservationController implements Initializable {

    public Pane card;
    public TextField txtName;
    public TextField txtSurname;
    public TextField txtEmail;
    public PasswordField txtPass;
    public Button btnSave;
    public Label lblConsoleProfile;
    public ComboBox<Barber> comboBarbers;
    public Pane resCard;
    public Pane resultCard;
    public DatePicker resDate;
    public ComboBox<Time> comboTime;
    public CheckComboBox<Operation> comboOp;
    public Label lblSelectedBarber;
    public Label lblSelectedDate;
    public Label lblSelectedTime;
    public Label lblSelectedOp;

    int cid;
    DataBaseOperations db = new DataBaseOperations();
    List<Time> allTimes;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        combo.setCellFactory();
//        comboBarbers.se

        setComboBoxesStyle();
        loadData();
    }

    private void loadData() {
        List<Barber> barbers = db.getBarbers();
        comboBarbers.getItems().addAll(barbers);

        allTimes = new LinkedList<>();
        List<String> temp = List.of("08:00:00", "08:30:00", "09:00:00", "09:30:00", "10:00:00", "10:30:00", "11:00:00", "11:30:00", "12:00:00",
                "12:30:00", "13:00:00", "13:30:00", "14:00:00", "14:30:00", "15:00:00", "15:30:00", "16:00:00", "16:30:00", "17:00:00", "17:30:00",
                "18:00:00", "18:30:00", "19:00:00", "19:30:00", "20:00:00");
        for (String s : temp) {
            allTimes.add(Time.valueOf(s));
        }
        List<Operation> ops = db.getOperations();
        comboOp.getItems().addAll(ops);

    }

    private void setComboBoxesStyle() {
        comboBarbers.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Barber> call(ListView<Barber> l) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Barber barber, boolean empty) {
                        super.updateItem(barber, empty);
                        if (barber == null || empty) {
                            setGraphic(null);
                        } else {
                            setText(barber.getName() + " " + barber.getSurname());
                        }
                    }
                };
            }
        });
        comboBarbers.setConverter(new StringConverter<>() {
            @Override
            public String toString(Barber barber) {
                if (barber == null) {
                    return null;
                } else {
                    return barber.getName() + " " + barber.getSurname();
                }
            }

            @Override
            public Barber fromString(String userId) {
                return null;
            }
        });
        resDate.setDayCellFactory(new Callback<>() {
            @Override
            public DateCell call(final DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item.getDayOfWeek() == DayOfWeek.SATURDAY
                                || item.getDayOfWeek() == DayOfWeek.SUNDAY
                        ) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }
                    }
                };
            }
        });
        comboOp.setConverter(new StringConverter<>() {
            @Override
            public String toString(Operation op) {
                if (op == null) {
                    return null;
                } else {
                    return op.getName();
                }
            }

            @Override
            public Operation fromString(String userId) {
                return null;
            }
        });

    }

    public void save(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(Objects.requireNonNull(this.getClass().getResource("media/error.png")).toString()));
        alert.setTitle("Warning!");
        alert.setHeaderText("Do you really want to save?");
        if (txtName.getText().equals("") || txtSurname.getText().equals("") || txtEmail.getText().equals("") || txtPass.getText().equals("")) {
            lblConsoleProfile.setTextFill(Color.web("#f84040"));
            lblConsoleProfile.setText("Some fields are empty!");
            return;
        }

        Pattern p = Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
        Matcher m = p.matcher(txtEmail.getText());
        if (!m.find()) {
            lblConsoleProfile.setTextFill(Color.web("#f84040"));
            lblConsoleProfile.setText("Invalid email!");
        } else {
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                db.updateCustomer(Attribute.NAME, txtName.getText(), cid);
                db.updateCustomer(Attribute.SURNAME, txtSurname.getText(), cid);
                db.updateCustomer(Attribute.EMAIL, txtEmail.getText(), cid);
                db.updateCustomer(Attribute.PASSWORD, txtPass.getText(), cid);
                lblConsoleProfile.setTextFill(Color.web("#42ba96"));
                lblConsoleProfile.setText("Saved!");
                txtPass.setText("");
            } else {
                event.consume();
            }
        }

    }

    public void book(ActionEvent event) {
        List<Integer> opIDs = new LinkedList<>();
        for (int i = 0; i < comboOp.getCheckModel().getCheckedItems().size(); i++) {
            opIDs.add(comboOp.getCheckModel().getCheckedItems().get(i).getId());
        }
        db.bookReservation(resDate.getValue().toString(), comboTime.getValue(), comboBarbers.getValue().getId(), cid, opIDs);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Done!");
        alert.setHeaderText("Well done, reservation is successful!");
        alert.showAndWait();




    }

    public void loadTime(ActionEvent event) {
        if (!(comboBarbers.getValue() == null || resDate.getValue() == null)) {
            List<Time> avTime = new LinkedList<>();
            List<Time> busyTimes = db.getBusyTimes(comboBarbers.getValue().getId(), resDate.getValue().toString());

            avTime.addAll(allTimes);
            avTime.removeAll(busyTimes);
            comboTime.getItems().addAll(avTime);

        }
        if (resDate.getValue() != null) {
            lblSelectedDate.setText(resDate.getValue().toString());
        }
        if (comboBarbers.getValue() != null) {
            lblSelectedBarber.setText(comboBarbers.getValue().getName() + " " + comboBarbers.getValue().getSurname());
        }

    }

    public void showTime(ActionEvent event) {
        if (comboTime.getValue() != null) {
            lblSelectedTime.setText(comboTime.getValue().toString());
        }
    }

    public void showOp(ActionEvent event) {
        if (!comboOp.getCheckModel().getCheckedItems().isEmpty()) {
            String s = "";
            for (int i = 1; i <= comboOp.getCheckModel().getCheckedItems().size(); i++) {
                if (i % 2 == 0)
                    s += comboOp.getCheckModel().getCheckedItems().get(i - 1).getName() + ", \n";
                else if (i == comboOp.getCheckModel().getCheckedItems().size())
                    s += comboOp.getCheckModel().getCheckedItems().get(i - 1).getName();
                else
                    s += comboOp.getCheckModel().getCheckedItems().get(i - 1).getName() + ", ";
            }
            lblSelectedOp.setText(s);
        }
    }

}