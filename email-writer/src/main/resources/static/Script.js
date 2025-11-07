const emailContent = document.getElementById("emailContent");
const tone = document.getElementById("tone");
const generateBtn = document.getElementById("generateBtn");
const generatedReply = document.getElementById("generatedReply");
const copyBtn = document.getElementById("copyBtn");
const errorDiv = document.getElementById("error");
const loadingDiv = document.getElementById("loading");

generateBtn.addEventListener("click", async () => {
  errorDiv.innerText = "";
  loadingDiv.style.display = "block";

  const payload = {
    emailContent: emailContent.value.trim(),
    tone: tone.value
  };

  try {
    const response = await fetch("http://localhost:8080/api/email/generate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    // Backend returns plain string, so use .text()
    const data = await response.text();
    generatedReply.value = data;  // show only latest reply
  } catch (err) {
    errorDiv.innerText = "Failed to generate reply.";
    console.error(err);
  } finally {
    loadingDiv.style.display = "none";
  }
});

copyBtn.addEventListener("click", () => {
  navigator.clipboard.writeText(generatedReply.value)
    .then(() => {
      alert("Text copied successfully ✅");
    })
    .catch(err => {
      console.error("Failed to copy: ", err);
    });
});

