// contacts.js

console.log("Contacts.js loaded");

// Use relative URL so it works on both localhost and production
const baseURL = window.location.origin; // e.g., http://localhost:8081 or https://www.scm20.site

// Get the modal element
const viewContactModal = document.getElementById("view_contact_modal");

// Define options for Flowbite modal
const options = {
  placement: "bottom-right",
  backdrop: "dynamic",
  backdropClasses: "bg-gray-900/50 dark:bg-gray-900/80 fixed inset-0 z-40",
  closable: true,
  onHide: () => {
    console.log("Modal is hidden");
  },
  onShow: () => {
    console.log("Modal is shown");
  },
  onToggle: () => {
    console.log("Modal toggled");
  },
};

// Instance options
const instanceOptions = {
  id: "view_contact_modal",      // must match the modal's id
  override: true,
};

// Create the modal instance (global variable)
let contactModal;

// Initialize modal only when the DOM is fully loaded
document.addEventListener("DOMContentLoaded", function() {
  if (viewContactModal) {
    contactModal = new Modal(viewContactModal, options, instanceOptions);
    console.log("Modal initialized");
  } else {
    console.warn("Modal element not found");
  }
});

// Function to open modal
function openContactModal() {
  if (contactModal) {
    contactModal.show();
  } else {
    console.error("Modal not initialized");
  }
}

// Function to close modal
function closeContactModal() {
  if (contactModal) {
    contactModal.hide();
  } else {
    console.error("Modal not initialized");
  }
}

// Load contact data and open modal
async function loadContactdata(id) {
  console.log("Loading contact ID:", id);
  try {
    const response = await fetch(`${baseURL}/api/contacts/${id}`);
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    const data = await response.json();
    console.log("Contact data:", data);

    // Populate modal fields
    document.querySelector("#contact_name").innerHTML = data.name || "N/A";
    document.querySelector("#contact_email").innerHTML = data.email || "N/A";
    document.querySelector("#contact_image").src = data.picture || "https://static-00.iconduck.com/assets.00/profile-default-icon-2048x2045-u3j7s5nj.png";
    document.querySelector("#contact_address").innerHTML = data.address || "Not provided";
    document.querySelector("#contact_phone").innerHTML = data.phoneNumber || "N/A";
    document.querySelector("#contact_about").innerHTML = data.description || "No description";

    // Favorite stars
    const contactFavorite = document.querySelector("#contact_favorite");
    if (data.favorite) {
      contactFavorite.innerHTML = '<i class="fas fa-star text-yellow-400"></i><i class="fas fa-star text-yellow-400"></i><i class="fas fa-star text-yellow-400"></i><i class="fas fa-star text-yellow-400"></i><i class="fas fa-star text-yellow-400"></i>';
    } else {
      contactFavorite.innerHTML = "Not a favorite contact";
    }

    // Website link
    const websiteLink = document.querySelector("#contact_website");
    if (data.websiteLink) {
      websiteLink.href = data.websiteLink;
      websiteLink.innerHTML = data.websiteLink;
      websiteLink.target = "_blank";
    } else {
      websiteLink.href = "#";
      websiteLink.innerHTML = "Not provided";
    }

    // LinkedIn link
    const linkedInLink = document.querySelector("#contact_linkedIn");
    if (data.linkedInLink) {
      linkedInLink.href = data.linkedInLink;
      linkedInLink.innerHTML = data.linkedInLink;
      linkedInLink.target = "_blank";
    } else {
      linkedInLink.href = "#";
      linkedInLink.innerHTML = "Not provided";
    }

    // Open the modal
    openContactModal();
  } catch (error) {
    console.error("Error loading contact:", error);
    alert("Could not load contact details. Please try again.");
  }
}

// Delete contact with SweetAlert confirmation
async function deleteContact(id) {
  Swal.fire({
    title: "Are you sure?",
    text: "You won't be able to revert this!",
    icon: "warning",
    showCancelButton: true,
    confirmButtonColor: "#d33",
    cancelButtonColor: "#3085d6",
    confirmButtonText: "Yes, delete it!"
  }).then((result) => {
    if (result.isConfirmed) {
      // Use relative URL to delete the contact
      window.location.href = `/user/contacts/delete/${id}`;
    }
  });
}