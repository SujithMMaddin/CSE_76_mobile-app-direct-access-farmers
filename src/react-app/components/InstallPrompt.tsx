import { useState } from 'react';
import { usePWA } from '@/react-app/hooks/usePWA';
import { Download, X, Smartphone } from 'lucide-react';

export default function InstallPrompt() {
  const { isInstallable, isInstalled, installApp } = usePWA();
  const [isDismissed, setIsDismissed] = useState(false);

  // Don't show if already installed, not installable, or dismissed
  if (isInstalled || !isInstallable || isDismissed) {
    return null;
  }

  const handleInstall = async () => {
    const success = await installApp();
    if (!success) {
      // Fallback instructions for manual installation
      alert('To install FarmDirect:\n\n1. Tap the share button in your browser\n2. Select "Add to Home Screen"\n3. Tap "Add" to install');
    }
  };

  return (
    <div className="fixed bottom-4 left-4 right-4 md:left-auto md:right-4 md:max-w-sm z-50">
      <div className="bg-white rounded-xl shadow-lg border border-gray-200 p-4">
        <button
          onClick={() => setIsDismissed(true)}
          className="absolute top-2 right-2 p-1 text-gray-400 hover:text-gray-600 transition-colors"
        >
          <X className="w-4 h-4" />
        </button>
        
        <div className="flex items-start space-x-3">
          <div className="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center flex-shrink-0">
            <Smartphone className="w-5 h-5 text-green-600" />
          </div>
          
          <div className="flex-1 min-w-0">
            <h3 className="font-semibold text-gray-900 mb-1">
              Install FarmDirect
            </h3>
            <p className="text-sm text-gray-600 mb-3">
              Add to your home screen for quick access and app-like experience
            </p>
            
            <button
              onClick={handleInstall}
              className="w-full bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-lg font-medium transition-colors flex items-center justify-center space-x-2"
            >
              <Download className="w-4 h-4" />
              <span>Install App</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
