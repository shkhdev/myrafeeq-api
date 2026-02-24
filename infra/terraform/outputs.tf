output "droplet_ip" {
  description = "Droplet public IP"
  value       = digitalocean_droplet.main.ipv4_address
}
