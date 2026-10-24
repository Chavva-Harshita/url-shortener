const form = document.querySelector("#shorten-form");
const urlInput = document.querySelector("#original-url");
const shortenButton = document.querySelector("#shorten-button");
const formMessage = document.querySelector("#form-message");
const resultCard = document.querySelector("#result-card");
const shortLink = document.querySelector("#short-link");
const copyButton = document.querySelector("#copy-button");
const copyMessage = document.querySelector("#copy-message");
const statsButton = document.querySelector("#stats-button");
const statsMessage = document.querySelector("#stats-message");

// Stats are scoped to the short link created in this browser tab.
let currentShortCode = "";

function setMessage(element, message, state = "") {
  element.textContent = message;
  if (state) {
    element.dataset.state = state;
  } else {
    delete element.dataset.state;
  }
}

function getFriendlyError(status, detail) {
  const message = typeof detail === "string" ? detail.toLowerCase() : "";

  if (message.includes("expired")) {
    return "That short link has expired.";
  }
  if (message.includes("valid") || message.includes("url cannot be empty")) {
    return "Please enter a valid URL starting with http:// or https://.";
  }
  if (status === 404) {
    return "We couldn’t find that short link. Check the code and try again.";
  }
  if (status >= 500) {
    return "The server is having a moment. Please try again shortly.";
  }
  if (!status) {
    return "We couldn’t reach snip. Check that the server is running and try again.";
  }
  return "We couldn’t complete that request. Please check the link and try again.";
}

async function readResponse(response) {
  try {
    return await response.json();
  } catch {
    return null;
  }
}

function normalizeUrl(value) {
  try {
    const parsed = new URL(value.trim());
    if (parsed.protocol !== "http:" && parsed.protocol !== "https:") {
      return null;
    }
    return parsed.href;
  } catch {
    return null;
  }
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  const originalUrl = normalizeUrl(urlInput.value);

  if (!originalUrl) {
    setMessage(formMessage, "Please enter a valid URL starting with http:// or https://.", "error");
    urlInput.focus();
    return;
  }

  shortenButton.disabled = true;
  shortenButton.querySelector(".button-label").textContent = "Making it tiny…";
  setMessage(formMessage, "Creating your short link…");
  setMessage(copyMessage, "");
  setMessage(statsMessage, "");
  resultCard.hidden = true;
  currentShortCode = "";

  try {
    const response = await fetch("/api/urls", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ originalUrl }),
    });
    const data = await readResponse(response);

    if (!response.ok) {
      setMessage(formMessage, getFriendlyError(response.status, data?.error), "error");
      return;
    }

    if (typeof data?.shortUrl !== "string" || typeof data?.shortCode !== "string") {
      setMessage(formMessage, "The server returned an unexpected response. Please try again.", "error");
      return;
    }

    currentShortCode = data.shortCode;
    shortLink.href = data.shortUrl;
    shortLink.textContent = data.shortUrl;
    resultCard.hidden = false;
    setMessage(formMessage, "Your link is ready to share.", "success");
  } catch {
    setMessage(formMessage, getFriendlyError(0), "error");
  } finally {
    shortenButton.disabled = false;
    shortenButton.querySelector(".button-label").textContent = "Shorten URL";
  }
});

copyButton.addEventListener("click", async () => {
  if (!shortLink.href) {
    setMessage(copyMessage, "Create a short link first.", "error");
    return;
  }

  try {
    await navigator.clipboard.writeText(shortLink.href);
    setMessage(copyMessage, "Copied! Your link is ready to paste.", "success");
  } catch {
    setMessage(copyMessage, "Couldn’t access the clipboard. You can select and copy the link above.", "error");
  }
});

statsButton.addEventListener("click", async () => {
  if (!currentShortCode) {
    setMessage(statsMessage, "Create a short link first.", "error");
    return;
  }

  statsButton.disabled = true;
  statsButton.querySelector(".stats-button-label").textContent = "Checking…";
  setMessage(statsMessage, "Loading click stats…");

  try {
    const response = await fetch(`/api/urls/${encodeURIComponent(currentShortCode)}/stats`);
    const data = await readResponse(response);

    if (!response.ok) {
      setMessage(statsMessage, getFriendlyError(response.status, data?.error), "error");
      return;
    }

    if (!Number.isFinite(data?.clickCount) || data.clickCount < 0) {
      setMessage(statsMessage, "The server returned unexpected click stats. Please try again.", "error");
      return;
    }

    const count = data.clickCount;
    const noun = count === 1 ? "click" : "clicks";
    setMessage(statsMessage, `${count.toLocaleString()} ${noun} so far`, "success");
  } catch {
    setMessage(statsMessage, getFriendlyError(0), "error");
  } finally {
    statsButton.disabled = false;
    statsButton.querySelector(".stats-button-label").textContent = "Check stats";
  }
});
