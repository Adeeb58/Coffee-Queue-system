/** @type {import('tailwindcss').Config} */
module.exports = {
    content: [
        "./src/**/*.{js,jsx,ts,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                primary: '#6F4E37',      // Coffee brown
                secondary: '#8B6F47',    // Light coffee brown
                accent: '#D4A574',       // Cream/latte color
            },
        },
    },
    plugins: [],
}