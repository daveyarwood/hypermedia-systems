const counterOutput = document.querySelector("#my-output");
const incrementButton = document.querySelector(".counter .increment-btn");

incrementButton.addEventListener("click", (_e) => {
  counterOutput.textContent++;
});
