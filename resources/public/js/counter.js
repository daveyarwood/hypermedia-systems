document.querySelectorAll("[data-counter]").forEach((el) => {
  const output = el.querySelector("[data-counter-output]");
  const increment = el.querySelector("[data-counter-increment]");

  increment.addEventListener("click", (_e) => {
    output.textContent++;
  });
});
