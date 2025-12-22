export default (function (condition, message) {
    if (condition && process.env.NODE_ENV !== 'production') {
        console.warn(message);
    }
});
//# sourceMappingURL=warning.js.map