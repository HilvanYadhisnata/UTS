import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.io.File;

public class StudentManagementApp extends Application {

    // Class untuk menyimpan data siswa
    public static class Student {
        private final SimpleIntegerProperty nim;
        private final SimpleStringProperty nama;
        private final SimpleDoubleProperty nilaiMatematika;
        private final SimpleDoubleProperty nilaiInggris;
        private final SimpleDoubleProperty nilaiProgramming;
        private final SimpleDoubleProperty nilaiAkhir;

        public Student(int nim, String nama, double nilaiMatematika, double nilaiInggris, double nilaiProgramming) {
            this.nim = new SimpleIntegerProperty(nim);
            this.nama = new SimpleStringProperty(nama);
            this.nilaiMatematika = new SimpleDoubleProperty(nilaiMatematika);
            this.nilaiInggris = new SimpleDoubleProperty(nilaiInggris);
            this.nilaiProgramming = new SimpleDoubleProperty(nilaiProgramming);
            
            // Nilai akhir adalah rata-rata dari 3 nilai mata pelajaran
            double nilai = (nilaiMatematika + nilaiInggris + nilaiProgramming) / 3.0;
            this.nilaiAkhir = new SimpleDoubleProperty(Math.round(nilai * 100.0) / 100.0);
        }

        public int getNim() {
            return nim.get();
        }

        public String getNama() {
            return nama.get();
        }

        public double getNilaiMatematika() {
            return nilaiMatematika.get();
        }

        public double getNilaiInggris() {
            return nilaiInggris.get();
        }

        public double getNilaiProgramming() {
            return nilaiProgramming.get();
        }

        public double getNilaiAkhir() {
            return nilaiAkhir.get();
        }
    }

    private TableView<Student> tableView;
    private ObservableList<Student> studentList;
    private TextField nimField, namaField, mathField, englishField, programmingField, searchField;
    private Label totalLabel, avgLabel;
    private final String FILE_PATH = "siswa.csv";

    @Override
    public void start(Stage primaryStage) {
        // Inisialisasi container utama
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Inisialisasi list
        studentList = FXCollections.observableArrayList();
        
        // Setup form untuk input data
        GridPane form = createInputForm();
        root.setTop(form);

        // Setup tabel
        setupTable();
        VBox centerContent = new VBox(10);
        
        // Search box
        HBox searchBox = new HBox(10);
        searchField = new TextField();
        searchField.setPromptText("Cari berdasarkan nama");
        Button searchButton = new Button("Cari");
        searchButton.setOnAction(e -> searchStudent());
        searchBox.getChildren().addAll(new Label("Cari:"), searchField, searchButton);

        // Sort controls
        Button sortButton = new Button("Urutkan berdasarkan Nilai Akhir");
        sortButton.setOnAction(e -> sortStudentsByFinalGrade());

        // Statistics
        HBox statsBox = new HBox(20);
        totalLabel = new Label("Total Nilai: 0");
        avgLabel = new Label("Rata-rata: 0");
        statsBox.getChildren().addAll(totalLabel, avgLabel);
        
        centerContent.getChildren().addAll(searchBox, tableView, sortButton, statsBox);
        root.setCenter(centerContent);
        
        // Load data dari file setelah semua UI elements diinisialisasi
        loadStudentsFromFile();
        updateStatistics();

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Aplikasi Manajemen Nilai Siswa");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private GridPane createInputForm() {
        GridPane form = new GridPane();
        form.setPadding(new Insets(10));
        form.setHgap(10);
        form.setVgap(10);

        nimField = new TextField();
        nimField.setPromptText("NIM");
        namaField = new TextField();
        namaField.setPromptText("Nama");
        mathField = new TextField();
        mathField.setPromptText("Nilai Matematika");
        englishField = new TextField();
        englishField.setPromptText("Nilai Bahasa Inggris");
        programmingField = new TextField();
        programmingField.setPromptText("Nilai Pemrograman");

        Button addButton = new Button("Tambah Siswa");
        addButton.setOnAction(e -> addStudent());
        
        Button saveButton = new Button("Simpan ke File");
        saveButton.setOnAction(e -> saveStudentsToFile());

        Button clearButton = new Button("Clear");
        clearButton.setOnAction(e -> clearForm());

        form.add(new Label("NIM:"), 0, 0);
        form.add(nimField, 1, 0);
        form.add(new Label("Nama:"), 0, 1);
        form.add(namaField, 1, 1);
        form.add(new Label("Nilai Matematika:"), 0, 2);
        form.add(mathField, 1, 2);
        form.add(new Label("Nilai Bahasa Inggris:"), 0, 3);
        form.add(englishField, 1, 3);
        form.add(new Label("Nilai Pemrograman:"), 0, 4);
        form.add(programmingField, 1, 4);

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(addButton, saveButton, clearButton);
        form.add(buttonBox, 1, 5);

        return form;
    }

    private void setupTable() {
        tableView = new TableView<>();
        tableView.setEditable(false);

        // Mendefinisikan kolom
        TableColumn<Student, Integer> nimCol = new TableColumn<>("NIM");
        nimCol.setCellValueFactory(new PropertyValueFactory<>("nim"));
        
        TableColumn<Student, String> namaCol = new TableColumn<>("Nama");
        namaCol.setCellValueFactory(new PropertyValueFactory<>("nama"));
        
        TableColumn<Student, Double> mathCol = new TableColumn<>("Matematika");
        mathCol.setCellValueFactory(new PropertyValueFactory<>("nilaiMatematika"));
        
        TableColumn<Student, Double> englishCol = new TableColumn<>("B. Inggris");
        englishCol.setCellValueFactory(new PropertyValueFactory<>("nilaiInggris"));
        
        TableColumn<Student, Double> programmingCol = new TableColumn<>("Pemrograman");
        programmingCol.setCellValueFactory(new PropertyValueFactory<>("nilaiProgramming"));
        
        TableColumn<Student, Double> finalCol = new TableColumn<>("Nilai Akhir");
        finalCol.setCellValueFactory(new PropertyValueFactory<>("nilaiAkhir"));

        // Menambahkan kolom ke tabel
        tableView.getColumns().addAll(nimCol, namaCol, mathCol, englishCol, programmingCol, finalCol);
        
        // Set tabel items
        tableView.setItems(studentList);
        
        // Mengizinkan sorting saat klik pada header kolom
        nimCol.setSortable(true);
        namaCol.setSortable(true);
        mathCol.setSortable(true);
        englishCol.setSortable(true);
        programmingCol.setSortable(true);
        finalCol.setSortable(true);
    }

    private void addStudent() {
        try {
            int nim = Integer.parseInt(nimField.getText());
            String nama = namaField.getText();
            double mathScore = Double.parseDouble(mathField.getText());
            double englishScore = Double.parseDouble(englishField.getText());
            double programmingScore = Double.parseDouble(programmingField.getText());

            // Validasi
            if (nama.isEmpty() || mathScore < 0 || mathScore > 100 || 
                englishScore < 0 || englishScore > 100 || 
                programmingScore < 0 || programmingScore > 100) {
                showAlert("Validasi Gagal", "Pastikan nama tidak kosong dan nilai antara 0-100");
                return;
            }

            Student newStudent = new Student(nim, nama, mathScore, englishScore, programmingScore);
            studentList.add(newStudent);
            clearForm();
            updateStatistics();
            
        } catch (NumberFormatException e) {
            showAlert("Format tidak valid", "Pastikan NIM dan Nilai diisi dengan angka");
        }
    }

    private void clearForm() {
        nimField.clear();
        namaField.clear();
        mathField.clear();
        englishField.clear();
        programmingField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadStudentsFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            System.out.println("File siswa.csv belum ada. Akan dibuat saat menyimpan data.");
            return; // Keluar dari method jika file tidak ditemukan
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 5) {
                    try {
                        int nim = Integer.parseInt(data[0]);
                        String nama = data[1];
                        double math = Double.parseDouble(data[2]);
                        double english = Double.parseDouble(data[3]);
                        double programming = Double.parseDouble(data[4]);
                        
                        Student student = new Student(nim, nama, math, english, programming);
                        studentList.add(student);
                    } catch (NumberFormatException e) {
                        System.err.println("Format data tidak valid: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Terjadi kesalahan saat membaca file: " + e.getMessage());
        }
        
        // Panggil updateStatistics hanya jika sudah ada UI elements yang diinisialisasi
        if (totalLabel != null && avgLabel != null) {
            updateStatistics();
        }
    }

    private void saveStudentsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Student student : studentList) {
                writer.write(String.format("%d,%s,%.2f,%.2f,%.2f%n", 
                    student.getNim(), 
                    student.getNama(),
                    student.getNilaiMatematika(),
                    student.getNilaiInggris(),
                    student.getNilaiProgramming()));
            }
            File file = new File(FILE_PATH);
            showInfoAlert("Sukses", "Data berhasil disimpan ke file " + file.getAbsolutePath());
        } catch (IOException e) {
            showAlert("Error", "Gagal menyimpan ke file: " + e.getMessage());
        }
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void searchStudent() {
        String searchTerm = searchField.getText().toLowerCase();
        if (searchTerm.isEmpty()) {
            tableView.setItems(studentList);
            return;
        }

        // Linear search untuk mencari nama (untuk binary search, perlu diurutkan terlebih dahulu)
        FilteredList<Student> filteredData = new FilteredList<>(studentList);
        filteredData.setPredicate(student -> 
            student.getNama().toLowerCase().contains(searchTerm) || 
            String.valueOf(student.getNim()).contains(searchTerm)
        );
        
        tableView.setItems(filteredData);
    }

    // Binary search untuk mencari siswa berdasarkan nama
    // Untuk contoh binary search, kita perlu memastikan data diurutkan sebelumnya
    private int binarySearchByName(List<Student> sortedList, String target) {
        int left = 0;
        int right = sortedList.size() - 1;
        
        while (left <= right) {
            int mid = left + (right - left) / 2;
            String midName = sortedList.get(mid).getNama().toLowerCase();
            int comparison = midName.compareTo(target.toLowerCase());
            
            if (comparison == 0) {
                return mid; // Found exact match
            } else if (comparison < 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        
        return -1; // Not found
    }

    private void sortStudentsByFinalGrade() {
        // Membuat kopi list untuk sorting
        List<Student> sortedList = new ArrayList<>(studentList);
        
        // Sort secara descending berdasarkan nilai akhir
        sortedList.sort(Comparator.comparing(Student::getNilaiAkhir).reversed());
        
        // Update observable list
        studentList.clear();
        studentList.addAll(sortedList);
        
        // Refresh tabel view
        tableView.refresh();
    }

    // Menghitung total nilai akhir menggunakan rekursi
    private double calculateTotalFinalGrade(List<Student> students, int index) {
        if (index < 0) {
            return 0;
        }
        return students.get(index).getNilaiAkhir() + calculateTotalFinalGrade(students, index - 1);
    }

    // Menghitung rata-rata nilai akhir
    private double calculateAverageFinalGrade() {
        if (studentList.isEmpty()) return 0;
        
        double total = calculateTotalFinalGrade(studentList, studentList.size() - 1);
        return total / studentList.size();
    }

    private void updateStatistics() {
        // Check apakah UI elements sudah diinisialisasi
        if (totalLabel == null || avgLabel == null) {
            return;
        }
        
        if (studentList.isEmpty()) {
            totalLabel.setText("Total Nilai: 0");
            avgLabel.setText("Rata-rata: 0");
            return;
        }
        
        double total = calculateTotalFinalGrade(studentList, studentList.size() - 1);
        double average = total / studentList.size();
        
        totalLabel.setText(String.format("Total Nilai: %.2f", total));
        avgLabel.setText(String.format("Rata-rata: %.2f", average));
    }

    public static void main(String[] args) {
        launch(args);
    }
}