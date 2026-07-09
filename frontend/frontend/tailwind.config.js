/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          blue: '#185FA5',
          lightblue: '#E6F1FB',
        }
      },
    },
  },
  plugins: [],
}