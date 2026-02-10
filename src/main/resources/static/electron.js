const { app, BrowserWindow, ipcMain } = require('electron');
const path = require('path');
const fs = require('fs');
const axios = require('axios');
const AdmZip = require('adm-zip');

// Đường dẫn lưu file offline
const OFFLINE_DIR = path.join(app.getPath('userData'), 'offline_lessons');
if (!fs.existsSync(OFFLINE_DIR)) {
  fs.mkdirSync(OFFLINE_DIR, { recursive: true });
}

let mainWindow;

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1200,
    height: 800,
    icon: path.join(__dirname, 'favicon.ico'), // Load icon App
    webPreferences: {
      nodeIntegration: true,    // QUAN TRỌNG: Cho phép React gọi Node.js
      contextIsolation: false,  // QUAN TRỌNG: Tắt bảo mật cô lập
      webSecurity: false,       // QUAN TRỌNG: Để load ảnh/file từ ổ cứng
    },
  });

  
  // Nếu App đã đóng gói (.exe) -> Load file index.html từ ổ cứng
  // Nếu đang code (Dev) -> Load localhost:3000
  if (app.isPackaged) {
    // Khi build xong, file electron.js sẽ nằm cùng cấp với index.html trong folder resources
    mainWindow.loadURL(`file://${path.join(__dirname, 'index.html')}`);
  } else {
    // Khi đang dev, load từ server React
    mainWindow.loadURL('http://localhost:3000');
    
    // Mở DevTools để debug cho dễ (Tùy chọn)
    // mainWindow.webContents.openDevTools();
  }
}


app.whenReady().then(createWindow);

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) {
    createWindow();
  }
});

ipcMain.handle('check-file-exists', async (event, { lessonId }) => {
  try {
    const folderPath = path.join(OFFLINE_DIR, `lesson_${lessonId}`);
    const indexPath = path.join(folderPath, 'index.html');
    
    // Kiểm tra xem file index.html đã có chưa
    if (fs.existsSync(indexPath)) {
      return `file://${indexPath}`; // Trả về đường dẫn nếu có
    }
    return null; // Trả về null nếu chưa có
  } catch (error) {
    return null;
  }
});

// ===== XỬ LÝ LOGIC TẢI & GIẢI NÉN (BACKEND) =====
ipcMain.handle('download-and-unzip', async (event, { url, lessonId }) => {
  try {
    const folderPath = path.join(OFFLINE_DIR, `lesson_${lessonId}`);
    const zipPath = path.join(OFFLINE_DIR, `temp_${lessonId}.zip`);
    const indexPath = path.join(folderPath, 'index.html');

    // 1. Nếu đã có file index.html -> Trả về luôn
    if (fs.existsSync(indexPath)) {
      console.log('File đã tồn tại, mở ngay:', indexPath);
      return `file://${indexPath}`;
    }

    console.log('Bắt đầu tải:', url);

    // 2. Tải file ZIP
    const response = await axios({
      method: 'GET',
      url: url,
      responseType: 'arraybuffer'
    });
    fs.writeFileSync(zipPath, response.data);

    // 3. Giải nén
    console.log('Đang giải nén...');
    const zip = new AdmZip(zipPath);
    zip.extractAllTo(folderPath, true);

    // 4. Xóa file rác
    fs.unlinkSync(zipPath);

    console.log('Hoàn tất:', indexPath);
    return `file://${indexPath}`;

  } catch (error) {
    console.error('Lỗi tải file:', error);
    throw error;
  }
  
});