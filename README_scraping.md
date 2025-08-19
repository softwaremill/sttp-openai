# OpenAI Model Capabilities Scraping

Due to Cloudflare protection on OpenAI's documentation pages, extracting model capabilities requires special approaches.

## 🚫 The Problem

OpenAI's documentation pages use:
- **Cloudflare bot protection** (returns "Just a moment..." challenge pages)
- **JavaScript-rendered content** (model details loaded dynamically)
- **HTTP 403 responses** for programmatic requests

## 🛠️ Solution Options

### Option 1: Playwright Browser Automation (Recommended)

Uses a real browser to handle JavaScript and Cloudflare challenges.

#### Setup Instructions:

1. **Install Playwright browsers** (one-time setup):
   ```bash
   # Install Playwright CLI globally
   npm install -g playwright
   
   # Or use npx
   npx playwright install
   
   # Install chromium specifically
   npx playwright install chromium
   ```

2. **Run the scraper**:
   ```bash
   scala-cli run model_capabilities_scraper.scala --jvm 17
   ```

#### What it does:
- Launches headless Chrome browser
- Handles Cloudflare challenges automatically
- Waits for JavaScript to render content
- Extracts model capabilities, context windows, training data
- Processes multiple model pages

### Option 2: Manual Browser + Developer Tools

If automation doesn't work, use this semi-manual approach:

1. **Open browser** and navigate to: `https://platform.openai.com/docs/models/gpt-4o`
2. **Wait** for page to fully load (Cloudflare challenge will resolve)
3. **Open Developer Tools** (F12)
4. **Run this JavaScript** in the console:

```javascript
// Extract model capabilities
const capabilities = [];
const text = document.body.innerText.toLowerCase();

// Check for common capabilities
const capabilityList = [
    'text generation', 'code generation', 'image understanding',
    'function calling', 'json mode', 'vision', 'multimodal'
];

capabilityList.forEach(cap => {
    if (text.includes(cap)) capabilities.push(cap);
});

// Extract metrics
const contextMatch = text.match(/(\d+[,\d]*)\s*tokens?\s*context|context.*?(\d+[,\d]*)\s*tokens?/);
const outputMatch = text.match(/(\d+[,\d]*)\s*output\s*tokens?/);
const trainingMatch = text.match(/training.*?through\s*([a-z]+\s*\d{4})|data.*?through\s*([a-z]+\s*\d{4})/);

console.log('🤖 Model Capabilities:', capabilities);
console.log('🧠 Context Window:', contextMatch ? contextMatch[1] || contextMatch[2] : 'Not found');
console.log('📝 Max Output:', outputMatch ? outputMatch[1] : 'Not found');
console.log('📚 Training Data:', trainingMatch ? trainingMatch[1] || trainingMatch[2] : 'Not found');
```

### Option 3: API + Static Data Approach

Combine API data with manually compiled capability information:

```bash
scala-cli run hybrid_model_info.scala
```

This approach:
1. Gets model list from OpenAI API
2. Matches models with pre-compiled capability data
3. Provides comprehensive information without web scraping

## 📊 Expected Output

Successfully scraping will give you information like:

```
🤖 GPT-4o
---------
  📋 Capabilities: Text generation, Code generation, Image understanding, Function calling
  🧠 Context Window: 128,000 tokens
  📚 Training Data: Through October 2023
  📝 Max Output: 4,096 tokens

🤖 DALL·E 3
-----------
  📋 Capabilities: Image generation, Text-to-image
  📝 Max Output: 1024×1024, 1024×1792, 1792×1024 pixels
```

## 🔧 Troubleshooting

### Playwright Issues:
- **"Browser not found"**: Run `npx playwright install chromium`
- **Timeout errors**: Increase timeout in the script (currently 60s)
- **Cloudflare still blocking**: Try running on a different IP/network

### Alternative Data Sources:
- **OpenAI API documentation**: Check `/docs/api-reference`
- **GitHub repositories**: Community-maintained model lists
- **Official blog posts**: Model announcement posts often contain capability details

## 💡 Pro Tips

1. **Rate limiting**: Add delays between requests to avoid triggering additional protection
2. **Headers**: Use realistic browser headers (already implemented)
3. **Caching**: Save successfully scraped data to avoid re-scraping
4. **Fallback**: Have a manual data backup for critical use cases

---

*Note: Web scraping should respect robots.txt and terms of service. This is for educational/research purposes.*
