const CACHE_NAME = "farmdirect-v2";
const urlsToCache = ["/", "/dashboard", "/manifest.json"];

// Install event - cache resources
self.addEventListener("install", (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => cache.addAll(urlsToCache))
  );
});

// Fetch event - serve from cache when offline
self.addEventListener("fetch", (event) => {
  // Skip SW interception in development to avoid blocking Vite HMR
  if (event.request.url.includes("localhost:5173")) {
    return;
  }

  event.respondWith(
    caches
      .match(event.request)
      .then((response) => {
        // Return cached version or fetch from network
        return response || fetch(event.request);
      })
      .catch(() => {
        // If both cache and network fail, return offline page for navigation
        if (event.request.mode === "navigate") {
          return caches.match("/").then((cachedResponse) => {
            return (
              cachedResponse ||
              new Response("<html><body>Offline</body></html>", {
                status: 200,
                headers: { "Content-Type": "text/html" },
              })
            );
          });
        }
        // For non-navigation requests, return a basic error response
        return new Response("Network error", { status: 503 });
      })
  );
});

// Activate event - clean up old caches
self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames.map((cacheName) => {
          if (cacheName !== CACHE_NAME) {
            return caches.delete(cacheName);
          }
        })
      );
    })
  );
});

// Handle push notifications (for future use)
self.addEventListener("push", (event) => {
  const options = {
    body: event.data ? event.data.text() : "New update available!",
    icon: "/manifest.json", // Fallback to manifest as icon placeholder
    badge: undefined,
    vibrate: [100, 50, 100],
    data: {
      dateOfArrival: Date.now(),
      primaryKey: 1,
    },
  };

  event.waitUntil(self.registration.showNotification("FarmDirect", options));
});

// Handle notification clicks
self.addEventListener("notificationclick", (event) => {
  event.notification.close();

  event.waitUntil(clients.openWindow("/"));
});
