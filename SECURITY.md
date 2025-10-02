# Security Policy

## Supported Versions

We actively support the following versions with security updates:

| Version | Supported          | Status |
| ------- | ------------------ | ------ |
| 1.0.x   | :white_check_mark: | Active |
| < 1.0   | :x:                | Not Supported (Pre-release) |

---

## Reporting a Vulnerability

**Please do NOT report security vulnerabilities through public GitHub issues.**

### Private Disclosure

We take security seriously and appreciate responsible disclosure. To report a security vulnerability:

1. **Email**: Send details to the project maintainers privately
   - Include "SECURITY" in the subject line
   - Provide detailed description of the vulnerability
   - Include steps to reproduce the issue
   - Suggest a fix if you have one

2. **Expected Response**:
   - **Acknowledgment**: Within 48 hours
   - **Initial Assessment**: Within 1 week
   - **Status Updates**: Weekly until resolved
   - **Resolution**: Critical issues within 2 weeks

3. **Information to Include**:
   - Type of vulnerability
   - Full paths of source files related to the issue
   - Location of the affected source code (tag/branch/commit)
   - Step-by-step instructions to reproduce
   - Proof-of-concept or exploit code (if possible)
   - Impact of the vulnerability
   - Suggested mitigation or fix

### What to Expect

- **Acknowledgment**: We'll confirm receipt within 48 hours
- **Investigation**: We'll investigate and assess severity
- **Fix Development**: Critical issues get immediate attention
- **Patch Release**: Security patches released as hotfixes
- **Credit**: You'll be credited (if desired) in release notes
- **Disclosure**: Coordinated public disclosure after fix

---

## Security Update Process

### For Critical Vulnerabilities

1. **Immediate Action**: Fix developed in private repository
2. **Testing**: Comprehensive testing of the fix
3. **Patch Release**: Released as soon as possible (target: < 2 weeks)
4. **Notification**: Security advisory published on GitHub
5. **Communication**: Announcement via GitHub Discussions and releases
6. **CVE**: Request CVE if applicable

### For Non-Critical Issues

1. **Assessment**: Evaluate severity and impact
2. **Prioritization**: Schedule fix for next release
3. **Fix Development**: Include in regular development cycle
4. **Release**: Include in next minor or patch release
5. **Documentation**: Document in release notes

---

## Security Best Practices for Users

### Dependency Management

- **Keep Updated**: Always use the latest stable version
- **Monitor Advisories**: Watch for security announcements
- **Review Dependencies**: Check transitive dependencies
- **Automate Updates**: Use dependency management tools

### Configuration Security

- **Validate Input**: Always validate user input before queries
- **Parameterized Queries**: Use built-in parameter handling (never string concatenation)
- **Least Privilege**: Database users should have minimal permissions
- **Connection Security**: Use encrypted connections to databases
- **Secrets Management**: Never hardcode credentials

### Example - Secure Usage

```java
// ✅ SECURE: Using parameterized queries
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .where("email", userInput);  // Automatically parameterized

// ❌ INSECURE: Don't do this
String sql = "SELECT * FROM users WHERE email = '" + userInput + "'";
```

---

## Security Features

### Built-in Protections

Query4j includes several security features:

1. **SQL Injection Prevention**
   - All queries use parameterized statements
   - No string concatenation of user input
   - Automatic parameter escaping

2. **Input Validation**
   - Field names validated against patterns
   - Operators validated against allowed list
   - Value types checked for compatibility

3. **Thread Safety**
   - Immutable design prevents race conditions
   - No shared mutable state
   - Safe for concurrent usage

4. **No Dynamic Code Execution**
   - No reflection-based vulnerabilities
   - No code generation at runtime
   - Compile-time type safety

---

## Known Security Considerations

### User Responsibilities

Users must ensure:

1. **Database Security**
   - Proper database user permissions
   - Network security for database connections
   - Regular database security updates

2. **Application Security**
   - Input validation at application boundaries
   - Authentication and authorization
   - Secure storage of credentials

3. **Deployment Security**
   - Secure configuration management
   - Regular security patches
   - Security monitoring and logging

---

## Security Audits

### Internal Audits

- **Frequency**: Before each major release
- **Scope**: Code review for security issues
- **Tools**: Static analysis, dependency scanning
- **Review**: Security-focused code review

### External Audits

- **Professional Audits**: Available for enterprise customers
- **Bug Bounty**: Contact us about bug bounty programs
- **Community**: Welcome security-focused reviews

---

## Vulnerability Disclosure Timeline

### Example Timeline

1. **Day 0**: Vulnerability reported privately
2. **Day 1-2**: Acknowledgment and initial assessment
3. **Day 3-7**: Investigation and fix development
4. **Day 8-14**: Testing and verification
5. **Day 15**: Patch release
6. **Day 15+**: Public disclosure

### Factors Affecting Timeline

- **Severity**: Critical issues get immediate attention
- **Complexity**: Complex fixes may take longer
- **Coordination**: May need to coordinate with dependencies
- **Testing**: Ensure fix doesn't break existing functionality

---

## Security Advisories

### Where to Find

- **GitHub Security Advisories**: https://github.com/query4j/dynamicquerybuilder/security/advisories
- **Release Notes**: Included in patch releases
- **Discussions**: Announced in GitHub Discussions

### Advisory Contents

- **Title**: Clear description of the issue
- **Severity**: CVSS score and rating
- **Affected Versions**: Which versions are vulnerable
- **Fixed Versions**: Versions containing the fix
- **Description**: Detailed explanation
- **Mitigation**: Workarounds if available
- **Credits**: Acknowledgment of reporter

---

## Security-Related Configuration

### Recommended Settings

```java
// Production security configuration
Query4jConfig config = Query4jConfig.builder()
    .core(CoreConfig.builder()
        .maxPredicateDepth(5)      // Limit query complexity
        .maxPredicateCount(50)      // Limit predicate count
        .defaultQueryTimeoutMs(30000) // Prevent long-running queries
        .queryStatisticsEnabled(true) // Enable monitoring
        .build())
    .build();
```

### Monitoring

Enable logging for security monitoring:

```java
// Log all queries for audit trail
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .where("email", userInput);

QueryStats stats = query.getExecutionStats();
logger.info("Query executed: {}", stats.getSql());
```

---

## Dependency Security

### Dependency Scanning

We regularly scan dependencies for vulnerabilities:

- **Tool**: Dependabot, Snyk, or similar
- **Frequency**: Daily automated scans
- **Action**: Immediate updates for critical issues
- **Communication**: Documented in release notes

### Minimal Dependencies

Query4j maintains minimal dependencies to reduce attack surface:
- Core: Lombok, SLF4J
- Test: JUnit, Mockito, jqwik
- No runtime dependencies on complex libraries

---

## Compliance and Standards

### Standards We Follow

- **OWASP Top 10**: Protection against common vulnerabilities
- **CWE**: Awareness of common weakness enumeration
- **Secure Coding**: Following Java secure coding guidelines
- **CVSS**: Using CVSS v3.1 for severity scoring

### Compliance Support

For organizations requiring specific compliance:
- **GDPR**: Data protection considerations
- **SOC 2**: Security control documentation
- **ISO 27001**: Information security management
- **Contact**: Reach out for compliance documentation

---

## Contact

### Security Team

For security-related inquiries:
- **General Questions**: GitHub Discussions (public, non-sensitive)
- **Vulnerability Reports**: Email maintainers privately
- **Emergency**: Mark email as URGENT for critical issues

### Response Commitment

- **Critical Issues**: Response within 24 hours
- **High Severity**: Response within 48 hours
- **Medium/Low**: Response within 1 week

---

## Acknowledgments

We thank the security researchers and community members who help keep Query4j secure. Responsible disclosure helps protect all users.

### Hall of Fame

Security researchers who have responsibly disclosed vulnerabilities will be acknowledged here (with permission).

*Currently none - v1.0.0 is the first release.*

---

## Updates to This Policy

This security policy may be updated periodically. Check back for changes or watch the repository for updates.

**Last Updated**: October 1, 2024  
**Version**: 1.0

---

**Thank you for helping keep Query4j and its users safe!** 🔒
