data "digitalocean_ssh_key" "main" {
  name = var.ssh_key_name
}

resource "digitalocean_project" "main" {
  name        = var.project_name
  description = "Project for MyRafeeq"
  purpose     = "Web Application"
  environment = var.environment
}

resource "digitalocean_droplet" "main" {
  name   = var.droplet_name
  region = var.region
  size   = var.droplet_size
  image  = var.droplet_image

  ssh_keys = [
    data.digitalocean_ssh_key.main.id,
  ]
  tags      = ["terraform", var.environment]
}

resource "digitalocean_project_resources" "main" {
  project   = digitalocean_project.main.id
  resources = [digitalocean_droplet.main.urn]
}
