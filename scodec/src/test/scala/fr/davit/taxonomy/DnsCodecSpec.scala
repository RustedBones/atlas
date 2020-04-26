package fr.davit.taxonomy

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scodec.bits._

class DnsCodecSpec extends AnyFlatSpec with Matchers {

  "DnsCodec" should "encode / decode dns header" in {
    val header = DnsHeader(
      id = 123,
      `type` = DnsType.Query,
      opCode = DnsOpCode.InverseQuery,
      isAuthoritativeAnswer = false,
      isTruncated = true,
      isRecursionDesired = false,
      isRecursionAvailable = true,
      responseCode = DnsResponseCode(15),
      countQuestions = 1,
      countAnswerRecords = 2,
      countAuthorityRecords = 3,
      countAdditionalRecords = 4
    )

    // format: off
                                 //                                1  1  1  1  1  1
    val expected =               //  0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
      bin"0000000001111011" ++   // |                      ID                       |
        bin"0000101010001111" ++ // |QR|   Opcode  |AA|TC|RD|RA|   Z    |   RCODE   |
        bin"0000000000000001" ++ // |                    QDCOUNT                    |
        bin"0000000000000010" ++ // |                    ANCOUNT                    |
        bin"0000000000000011" ++ // |                    NSCOUNT                    |
        bin"0000000000000100"    // |                    ARCOUNT                    |
    // format: on

    val data = DnsCodec.dnsHeaderCodec.encode(header).require
    data.size shouldBe 16 * 6
    data shouldBe expected

    DnsCodec.dnsHeaderCodec.decode(data).require.value shouldBe header
  }

  it should "encode / decode domain name" in {
    val name = "www.example.com"

    val expected =
      (ByteVector(3: Byte) ++ ByteVector("www".getBytes(DnsCodec.ascii)) ++
        ByteVector(7: Byte) ++ ByteVector("example".getBytes(DnsCodec.ascii)) ++
        ByteVector(3: Byte) ++ ByteVector("com".getBytes(DnsCodec.ascii)) ++
        ByteVector(0)).toBitVector // nul

    val data = DnsCodec.qName.encode(name).require

    data.size shouldBe (4 + (3 + 7 + 3)) * 8
    data shouldBe expected

    DnsCodec.qName.decode(data).require.value shouldBe name
  }

}
