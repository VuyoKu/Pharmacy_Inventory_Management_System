===============================================================
  HEALTHFIRST PHARMACY INVENTORY MANAGEMENT SYSTEM (PIMS)
  Programming 732 Assignment – User Manual
===============================================================

AUTHOR    : Vuyo Remmone Kube
STUDENT # : 402411204
DATE      : 2026

---------------------------------------------------------------
  TABLE OF CONTENTS
---------------------------------------------------------------
  1. System Requirements
  2. Database Setup
  3. Running the Application
  4. Default Login Credentials
  5. Module Guide – Admin
  6. Module Guide – Cashier
  7. Troubleshooting
  8. Project Structure

---------------------------------------------------------------
  1. SYSTEM REQUIREMENTS
---------------------------------------------------------------
  - Java JDK 17 or later     (https://adoptium.net)
  - MySQL Server 8.0 or later (https://dev.mysql.com/downloads)
  - MySQL Connector/J 8.x JAR (bundled in /lib)
  - Minimum 512 MB RAM
  - Screen resolution: 1280 × 720 or higher

---------------------------------------------------------------
  2. DATABASE SETUP
---------------------------------------------------------------
  Step 1: Start MySQL Server.
  Step 2: Open MySQL Workbench or CLI and run:

          source /path/to/database.sql

  Step 3: Confirm the following tables were created:
          users | suppliers | medicines | sales | sale_items

  Step 4: Verify default credentials work:
          SELECT * FROM users;

  Note: The database name is "healthfirst_pims".
        Default MySQL user is "root" / password "root".
        Edit DatabaseConnection.java to change credentials.

---------------------------------------------------------------
  3. RUNNING THE APPLICATION
---------------------------------------------------------------
  Option A – Using the executable (.exe):
    1. Ensure MySQL is running.
    2. Double-click "yourname_pims.exe"

  Option B – From source (using IntelliJ IDEA):
    1. Open the project in IntelliJ IDEA.
    2. Add /lib/mysql-connector-j-8.x.xx.jar to the classpath
       (File → Project Structure → Libraries → Add JAR).
    3. Run pims.Main

  Option C – Command Line:
    javac -cp ".;lib/mysql-connector-j-8.x.xx.jar" src/pims/*.java
    java  -cp ".;lib/mysql-connector-j-8.x.xx.jar;src" pims.Main

---------------------------------------------------------------
  4. DEFAULT LOGIN CREDENTIALS
---------------------------------------------------------------
  ADMINISTRATOR:
    Username : admin
    Password : admin123

  CASHIER 1:
    Username : cashier
    Password : cash123

  CASHIER 2:
    Username : cashier2
    Password : cash456

  After first login, the admin can create additional users
  via Admin Dashboard → Users tab.

---------------------------------------------------------------
  5. MODULE GUIDE – ADMIN
---------------------------------------------------------------
  After logging in as Admin, the Admin Dashboard opens with
  the following tabs (on the left sidebar):

   Dashboard
  ─────────────
  Overview of key metrics:
  • Total medicines in inventory
  • Total suppliers
  • Sales processed today
  • Number of items at low stock

   Medicines
  ─────────────
  Manage the medicine inventory.
  • ADD: Click "+ Add Medicine" → fill in the form → Save.
  • EDIT: Select a row → click " Edit" → update → Update.
  • DELETE: Select a row → click "✖ Delete" → confirm.
  • SEARCH: Enter text in the search box → click "Search".
  • Fields: Name, Company, Type, Price, Qty, Reorder Level,
            Expiry Date, Supplier.

   Suppliers
  ─────────────
  Manage supplier contacts.
  • Full CRUD operations (same workflow as Medicines).
  • Fields: Company Name, Contact Person, Phone, Email, Address.

   Users
  ─────────
  Manage cashier (and admin) accounts.
  • CREATE: Click "+ Create User" → fill form → Create.
  • DELETE: Select cashier row → click "✖ Delete".
  • RESET PASSWORD: Select a user → click "Reset Password".
  • Admins cannot delete other Admin accounts.
  • You cannot delete your own account.

   Reports
  ───────────
  Four built-in reports:
  a) Sales Report     – All sales with optional date filter.
  b) Item-Wise Sales  – Revenue per medicine, sorted by best seller.
  c) Low Stock Alert  – Medicines at or below reorder level.
  d) Expiry Report    – Medicines expiring within the next 30 days.

---------------------------------------------------------------
  6. MODULE GUIDE – CASHIER
---------------------------------------------------------------
  After logging in as a Cashier, the POS Dashboard opens.

   Point of Sale (POS)
  ───────────────────────
  • SELECT medicine from the dropdown (shows name, type, price,
    stock count).
  • SET quantity in the "Qty" box (default: 1).
  • Click "Add to Cart " to add the item.
  • Repeat to add more medicines.
  • REMOVE ITEM: Select a cart row → click "Remove Selected".
  • CLEAR CART: Click "Clear Cart 🗑" to start fresh.
  • CHECKOUT: Click "Checkout & Bill ✔" to process the sale.
    - Stock is automatically decremented.
    - A Bill Window appears with the printable receipt.
  • CHECK STOCK: Click "Check Stock" to view details without
    adding to the cart.

  Bill Window Actions:
  •  Print – sends to system printer.
  •  Save as Text – saves a .txt receipt file.
  •  Close – closes the window.

   Stock Check
  ───────────────
  • View all medicines with availability status.
  • Search by name or type.
  • Read-only – cashiers cannot edit inventory.

---------------------------------------------------------------
  7. TROUBLESHOOTING
---------------------------------------------------------------
   "Database error" on login:
    → Ensure MySQL is running.
    → Confirm database.sql was executed.
    → Check DatabaseConnection.java credentials.

   Medicine list is empty in POS:
    → Verify medicines table has data with qty > 0.
    → Click the "↺ Refresh" button in the Medicine panel.

   "Driver not found":
    → Add mysql-connector-j JAR to classpath.

   Sale checkout fails:
    → Ensure the medicine still has stock.
    → A transaction rollback is performed on error.

---------------------------------------------------------------
  8. PROJECT STRUCTURE
---------------------------------------------------------------
  PIMS/
  ├── src/
  │   └── pims/
  │       ├── Main.java                  ← Application entry point
  │       ├── auth/
  │       │   └── LoginFrame.java        ← Login screen
  │       ├── admin/
  │       │   ├── AdminDashboard.java    ← Admin shell & stats
  │       │   ├── MedicinePanel.java     ← Medicine CRUD
  │       │   ├── SupplierPanel.java     ← Supplier CRUD
  │       │   └── UserPanel.java         ← User management
  │       ├── cashier/
  │       │   ├── CashierDashboard.java  ← Cashier shell
  │       │   ├── POSPanel.java          ← Point of Sale
  │       │   ├── BillWindow.java        ← Receipt/bill dialog
  │       │   └── StockCheckPanel.java   ← Read-only stock view
  │       ├── reports/
  │       │   └── ReportsPanel.java      ← 4 report types
  │       ├── models/
  │       │   ├── User.java
  │       │   ├── Medicine.java
  │       │   ├── Supplier.java
  │       │   ├── Sale.java
  │       │   └── SaleItem.java
  │       ├── db/
  │       │   └── DatabaseConnection.java ← MySQL singleton
  │       └── utils/
  │           ├── UITheme.java            ← Design system
  │           └── SessionManager.java     ← Login session
  ├── database.sql    ← Full DB schema + seed data
  ├── screenshots/    ← Required screenshots folder
  └── README.txt      ← This file

===============================================================
  END OF USER MANUAL
===============================================================
