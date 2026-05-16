const overflowMenu = (tree = document) => {
  tree.querySelectorAll("[data-overflow-menu]").forEach((menuRoot) => {
    const button = menuRoot.querySelector("[aria-haspopup]"),
      menu = menuRoot.querySelector("[role=menu]"),
      items = [...menu.querySelectorAll("[role=menuitem]")];

    // Make menu items non-tabbable by default, so we can manage their focus
    // ourselves.
    items.forEach((item) => item.setAttribute("tabindex", "-1"));

    const isOpen = () => !menu.hidden;

    const toggleMenu = (open = !isOpen()) => {
      if (open) {
        menu.hidden = false;
        button.setAttribute("aria-expanded", "true");
        items[0].focus();
      } else {
        menu.hidden = true;
        button.setAttribute("aria-expanded", "false");
      }
    };

    // Initialize element attributes
    toggleMenu(isOpen());

    // Toggle menu when button is clicked
    button.addEventListener("click", () => toggleMenu());

    // Close menu when focus moves away
    menuRoot.addEventListener("blur", (_e) => toggleMenu(false));

    const clickAway = (e) => {
      // Hacky, manual garbage collection for event listeners for buttons that
      // aren't connected anymore.
      if (!menuRoot.isConnected) {
        window.removeEventListener("click", clickAway);
      }
      if (!menuRoot.contains(e.target)) {
        toggleMenu(false);
      }
    };
    window.addEventListener("click", clickAway);

    // Keyboard interactions for dropdown menu
    const currentIndex = () => {
      const i = items.indexOf(document.activeElement);
      return i == -1 ? 0 : i;
    };
    menu.addEventListener("keydown", (e) => {
      switch (e.key) {
        case "ArrowUp":
          items[currentIndex() - 1]?.focus();
          break;
        case "ArrowDown":
          items[currentIndex() + 1]?.focus();
          break;
        case "Space":
          items[currentIndex()].click();
          break;
        case "Home":
          items[0].focus();
          break;
        case "End":
          items[items.length - 1].focus();
          break;
        case "Escape":
          toggleMenu(false);
          button.focus();
          break;
      }
    });
  });
};

addEventListener("htmx:load", (e) => overflowMenu(e.target));
