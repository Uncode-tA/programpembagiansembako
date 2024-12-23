import java.sql.*;
import java.util.*;
import java.text.SimpleDateFormat;

//Interface SembakoManagement yang mendefinisikan metode untuk manajemen penerima
interface SembakoManagement {
    void addRecipient(String name, String address, int familySize);
    void displayRecipients();
    void updateRecipient(int id, String newName, int newFamilySize);
    void deleteRecipient(int id);
}

//Kelas Recipient sebagai superclass
class Recipient {
    protected String name;
    protected String address;
    protected int familySize;

    // Constructor untuk menginisialisasi nama, alamat, dan jumlah keluarga
    public Recipient(String name, String address, int familySize) {
        this.name = name;
        this.address = address;
        this.familySize = familySize;
    }

    // Method untuk mendapatkan detail penerima
    public String getDetails() {
        return "Nama: " + name + ", Alamat: " + address + ", Jumlah Keluarga: " + familySize;
    }
}

//Kelas SpecialRecipient sebagai subclass dari Recipient
class SpecialRecipient extends Recipient {
    private String category;

    // Constructor untuk menginisialisasi penerima dengan kategori khusus
    public SpecialRecipient(String name, String address, int familySize, String category) {
        super(name, address, familySize); // Memanggil konstruktor dari kelas induk
        this.category = category;
    }

    // Override method getDetails untuk menambahkan kategori pada detail penerima
    @Override
    public String getDetails() {
        return super.getDetails() + ", Kategori: " + category;
    }
}

//Kelas SembakoManager yang mengimplementasikan interface SembakoManagement
class SembakoManager implements SembakoManagement {
    private Connection connection; // Koneksi ke database
    private List<Recipient> recipients;  // Collection Framework: List untuk menyimpan penerima sementara

    // Konstruktor untuk membuka koneksi ke database
    public SembakoManager() {
        try {
            // Membuka koneksi ke database MySQL
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pembagiansembako_db", "root", "");
            System.out.println("Koneksi ke database berhasil.");
            recipients = new ArrayList<>();  // Menginisialisasi list untuk menyimpan penerima secara sementara
        } catch (SQLException e) {
            System.err.println("Koneksi gagal: " + e.getMessage());
            e.printStackTrace(); // Log lebih lengkap untuk debugging
        }
    }

    //Method untuk menambahkan penerima baru
    @Override
    public void addRecipient(String name, String address, int familySize) {
        try {
            // Memeriksa apakah penerima sudah ada di database
            String checkQuery = "SELECT * FROM recipients WHERE name = ? AND address = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setString(1, name);
            checkStmt.setString(2, address);
            ResultSet checkRs = checkStmt.executeQuery();

            if (checkRs.next()) {
                System.out.println("Data penerima sudah ada di database.");
            } else {
                // Menambahkan penerima ke dalam list sementara
                Recipient newRecipient = new Recipient(name, address, familySize);
                recipients.add(newRecipient);  // Menambahkan ke list di memori

                // Menambahkan penerima ke database
                String query = "INSERT INTO recipients (name, address, family_size) VALUES (?, ?, ?)";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1, name);
                stmt.setString(2, address);
                stmt.setInt(3, familySize);
                stmt.executeUpdate();
                System.out.println("Penerima berhasil ditambahkan.");
            }
        } catch (SQLException e) {
            System.err.println("Gagal menambahkan penerima: " + e.getMessage());
            e.printStackTrace(); // Log error lebih detail
        }
    }

    //Method untuk menampilkan semua penerima
    @Override
    public void displayRecipients() {
        try {
            // Query untuk mengambil data penerima dari database
            String query = "SELECT * FROM recipients";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            System.out.println("\nDaftar Penerima Sembako:");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String address = rs.getString("address");
                int familySize = rs.getInt("family_size");
                Timestamp timestamp = rs.getTimestamp("added_date");

                // Perhitungan jumlah sembako berdasarkan ukuran keluarga
                int sembakoQty = familySize * 3;
                System.out.println(id + ". " + name + " - " + address);
                System.out.println("Jumlah Keluarga: " + familySize + ", Sembako: " + sembakoQty + " item");
                System.out.println("Tanggal Pendaftaran: " + sdf.format(timestamp));
            }

            // Menampilkan penerima dari list sementara (Collection Framework)
            System.out.println("\nDaftar Penerima Sembako (in-memory):");
            for (Recipient recipient : recipients) {
                System.out.println(recipient.getDetails());
            }
        } catch (SQLException e) {
            System.err.println("Gagal menampilkan data penerima: " + e.getMessage());
            e.printStackTrace(); 
        }
    }

    // 7. Method untuk memperbarui data penerima
    @Override
    public void updateRecipient(int id, String newName, int newFamilySize) {
        try {
            // Memeriksa apakah penerima dengan ID yang diberikan ada di database
            String checkQuery = "SELECT * FROM recipients WHERE id = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setInt(1, id);
            ResultSet checkRs = checkStmt.executeQuery();

            if (!checkRs.next()) {
                System.out.println("Data tidak ditemukan untuk ID: " + id);
                return; // Keluar dari metode jika data tidak ditemukan
            }

            // Memperbarui data penerima di database
            String query = "UPDATE recipients SET name = ?, family_size = ? WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, newName);
            stmt.setInt(2, newFamilySize);
            stmt.setInt(3, id);
            stmt.executeUpdate();

            // Memperbarui data di list sementara
            for (Recipient recipient : recipients) {
                if (recipient.name.equals(newName)) {
                    recipient.name = newName;
                    recipient.familySize = newFamilySize;
                }
            }

            System.out.println("Data penerima berhasil diperbarui.");
            System.out.println("Data yang diperbarui: " + newName + ", Jumlah Keluarga: " + newFamilySize);
        } catch (SQLException e) {
            System.err.println("Gagal memperbarui data: " + e.getMessage());
            e.printStackTrace(); 
        }
    }

    // 8. Method untuk menghapus data penerima
    @Override
    public void deleteRecipient(int id) {
        try {
            // Memeriksa apakah penerima dengan ID yang diberikan ada di database
            String checkQuery = "SELECT * FROM recipients WHERE id = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setInt(1, id);
            ResultSet checkRs = checkStmt.executeQuery();

            if (!checkRs.next()) {
                System.out.println("Data tidak ditemukan untuk ID: " + id);
                return; // Keluar dari metode jika data tidak ditemukan
            }

            // Mengambil nama penerima untuk penghapusan dari list sementara
            String nameToDelete = checkRs.getString("name");

            // Menghapus data penerima dari database
            String query = "DELETE FROM recipients WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, id);
            stmt.executeUpdate();

            // Menghapus penerima dari list sementara
            recipients.removeIf(recipient -> recipient.name.equals(nameToDelete));

            System.out.println("Data penerima berhasil dihapus.");
        } catch (SQLException e) {
            System.err.println("Gagal menghapus data: " + e.getMessage());
            e.printStackTrace(); // Log error lebih detail
        }
    }

    // Main method untuk menjalankan aplikasi
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SembakoManager manager = new SembakoManager();

        while (true) {
            try {
                System.out.println("\n=== Menu Manajemen Sembako ===");
                System.out.println("1. Tambah Penerima");
                System.out.println("2. Tampilkan Semua Penerima");
                System.out.println("3. Perbarui Data Penerima");
                System.out.println("4. Hapus Penerima");
                System.out.println("5. Keluar");
                System.out.print("Pilih opsi: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); 

                switch (choice) {
                    case 1:
                        System.out.print("Nama: ");
                        String name = scanner.nextLine();
                        System.out.print("Alamat: ");
                        String address = scanner.nextLine();
                        System.out.print("Jumlah Keluarga: ");
                        int familySize = scanner.nextInt();
                        manager.addRecipient(name, address, familySize);
                        break;
                    case 2:
                        manager.displayRecipients();
                        break;
                    case 3:
                        System.out.print("ID: ");
                        int id = scanner.nextInt();
                        scanner.nextLine();
                        System.out.print("Nama Baru: ");
                        String newName = scanner.nextLine();
                        System.out.print("Jumlah Keluarga Baru: ");
                        int newFamilySize = scanner.nextInt();
                        manager.updateRecipient(id, newName, newFamilySize);
                        break;
                    case 4:
                        System.out.print("ID: ");
                        int deleteId = scanner.nextInt();
                        manager.deleteRecipient(deleteId);
                        break;
                    case 5:
                        System.out.println("Program selesai. Terima kasih!");
                        scanner.close();
                        System.exit(0);
                    default:
                        System.out.println("Pilihan tidak valid.");
                }
            } catch (InputMismatchException e) {
                System.err.println("Input tidak valid. Silakan coba lagi.");
                scanner.nextLine(); // Untuk membersihkan input yang tidak valid
            } catch (Exception e) {
                System.err.println("Terjadi kesalahan: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
