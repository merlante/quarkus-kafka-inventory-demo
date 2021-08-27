terraform {
  required_providers {
    rhoas = {
      source  = "pmuir/rhoas"
    }
    kafka = {
      source = "Mongey/kafka"
      version = "0.2.12"
    }
  }
}

provider "rhoas" {}

provider "kafka" {
  bootstrap_servers = [ rhoas_kafka.inventory-demo.kafka[0].bootstrap_server_host ]
  tls_enabled = true
  sasl_username = rhoas_service_account.inventory-demo.service_account[0].client_id
  sasl_password = rhoas_service_account.inventory-demo.service_account[0].client_secret
}

resource "rhoas_kafka" "inventory-demo" {
  kafka {
    name = "my-inventory-demo"
  }
}

resource "rhoas_service_account" "inventory-demo" {
  service_account {
    name = "my-inventory-demo-service-account"
    description = "Service account for inventory demo."
  }
}

resource "kafka_topic" "orders" {
  name = "orders"
  partitions = 3
  replication_factor = 1
  config = {
    "cleanup.policy" = "delete"
  }
}

resource "kafka_topic" "shipments" {
  name = "shipments"
  partitions = 3
  replication_factor = 1
  config = {
    "cleanup.policy" = "delete"
  }
}

resource "kafka_topic" "stock-levels" {
  name = "stock-levels"
  partitions = 3
  replication_factor = 1
  config = {
    "cleanup.policy" = "delete"
  }
}

resource "kafka_topic" "reserved-stock" {
  name = "reserved-stock"
  partitions = 3
  replication_factor = 1
  config = {
    "cleanup.policy" = "delete"
  }
}

resource "kafka_topic" "available-stock" {
  name = "available-stock"
  partitions = 3
  replication_factor = 1
  config = {
    "cleanup.policy" = "delete"
  }
}