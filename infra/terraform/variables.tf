variable "do_token" {
  description = "DigitalOcean API token"
  type        = string
  sensitive   = true
}

variable "project_name" {
  description = "Project name"
  type        = string
  default     = "MyRafeeq"
}

variable "environment" {
  description = "Environment (development staging production)"
  type        = string
  default     = "production"
}

variable "region" {
  description = "DigitalOcean region"
  type        = string
  default     = "fra1"
}

variable "droplet_name" {
  description = "Droplet name"
  type        = string
  default     = "app-server"
}

variable "droplet_image" {
  description = "Droplet image"
  type        = string
  default     = "ubuntu-24-04-x64"
}

variable "droplet_size" {
  description = "Droplet size"
  type        = string
  default     = "s-2vcpu-4gb"
}

variable "ssh_key_name" {
  description = "SSH key name in DigitalOcean"
  type        = string
  default     = "myrafeeq-app-server-key"
}
