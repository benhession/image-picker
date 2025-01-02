# local-env-queue-helper

Simple service to imitate the AWS event source mapping when running the services locally.
The service polls the localstack sqs queue(s) at a given interval and forwards the messages to the
appropriate lambda service.

---

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.
