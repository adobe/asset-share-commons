document.addEventListener('DOMContentLoaded', function() {
    const images = document.querySelectorAll('img[data-asset-share-missing-image]');
    images.forEach(img => {
        img.onerror = function() {
            this.src = this.getAttribute('data-asset-share-missing-image');
        };
    });
});